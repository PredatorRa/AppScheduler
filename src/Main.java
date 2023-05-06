import java.io.*;
import java.util.*;

/*

读取端口文件和流文件，并创建 Scheduler 对象进行调度
*/
public class Main {
    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        // 读取端口文件和流文件
        int i=0;
        List<Port> ports = null;
        List<Flow> flows = null;
        while(true) {
            ports = readPorts(String.format("../data/%d/port.txt",i ));
            flows = readFlows(String.format("../data/%d/flow.txt",i ));
            if(ports==null||ports.isEmpty()){
                break;
            }
            // 创建 Scheduler 对象进行调度
//            Scheduler scheduler = new Scheduler(ports, flows);
//            scheduler.run();
            test(ports,flows);
            writeFile(flows,i);
            i++;
            System.out.println(i+":"+String.valueOf(System.currentTimeMillis()-t1));
        }
    }

    private static void test(List<Port> ports, List<Flow> flows) {
        Collections.sort(flows);
        int i = 0 ,k=0;
        int[] startTime = new int[ports.size()];
        while(i<flows.size()){
            Flow flow = flows.get(i);
            for(int j=0;j<ports.size();j++) {
                if(ports.get(k).getBandwidth()<flow.getBandwidth()){
                    k = (k+1)%ports.size();
                    continue;
                }
                int maxTime = Math.max(flow.getEnterTime(), startTime[k]);
                flow.setPortId(ports.get(k).getId());
                flow.setSendTime(maxTime);
                startTime[j] = maxTime + flow.getDuration();
                k = (k+1)%ports.size();
                break;
            }
            i++;
        }
    }

    private static List<Flow> readFlows(String flowFile) {
        List<Flow> flowList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(flowFile))) {
            // 跳过第一行
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                int flowId = Integer.parseInt(data[0]);
                int bandwidth = Integer.parseInt(data[1]);
                int arrivalTime = Integer.parseInt(data[2]);
                int sendTime = Integer.parseInt(data[3]);
                flowList.add(new Flow(flowId, bandwidth, arrivalTime, sendTime));
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return flowList;
    }

    private static List<Port> readPorts(String portFile) {
        List<Port> portList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(portFile))) {
            // 跳过第一行
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                int portId = Integer.parseInt(data[0]);
                int bandwidth = Integer.parseInt(data[1]);
                portList.add(new Port(portId, bandwidth));
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return portList;
    }

    //把结果写入文件
    private static void writeFile(List<Flow> flows,int i) {
        try {
            File outputFile = new File(String.format("../data/%d/result.txt", i));
            PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, false));
            for (Flow flow : flows) {
                writer.println(flow.getId() + "," + flow.getPortId()  + "," + flow.getSendTime());
            }
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}


/*

流对象，记录流的带宽、进入设备时间和发送时间
*/
class Flow implements Comparable<Flow> {
    private final int id;
    private final int bandwidth;
    private final int enterTime;
    private final int duration;
    //发送时间
    private int sendTime;
    //发送端口
    private int portId;

    public Flow(int id, int bandwidth, int enterTime, int duration) {
        this.id = id;
        this.bandwidth = bandwidth;
        this.enterTime = enterTime;
        this.duration = duration;
        this.sendTime = -1;
        this.portId = -1;
    }

    public int getId() {
        return this.id;
    }

    public int getBandwidth() {
        return this.bandwidth;
    }

    public int getEnterTime() {
        return this.enterTime;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public int getPortId() {
        return this.portId;
    }

    public void setPortId(int portId) {
        this.portId = portId;
    }

    @Override
    public int compareTo(Flow o) {
        return this.enterTime - o.enterTime;
    }
}

/*
 * 端口对象，记录出端口的带宽和占用情况
 */
class Port {
    private final int id;
    private final int bandwidth;
    public int[] timeLine;

    public Port(int id, int bandwidth) {
        this.id = id;
        this.bandwidth = bandwidth;
        this.timeLine = new int[10000];
        Arrays.fill(timeLine, bandwidth);
    }

    // 初始化端口状态
    public void init() {
        Arrays.fill(timeLine, bandwidth);
    }

    /**
     * 判断当前时间是否可用,可用返回当前时间段最小带宽,不可用返回Integer.MIN_VALUE
     */
    public int isAvailable(int startTime, int duration, int useWidth) {
        //判断当前时间段容量是否够 O(t)
        int minWidth = Integer.MIN_VALUE;
        for (int i = startTime; i < startTime + duration; i++) {
            if (timeLine[i] < useWidth) return -1;
            if (timeLine[i] < minWidth) minWidth = timeLine[i];
        }
        return minWidth;
    }

    /**
     * 找到下一段有空容量的period O(t)
     * @param enterTime
     * @param duration
     * @param bandwidth
     * @return 开始时间
     */
    public int findNextAvailablePeriod(int enterTime, int duration, int bandwidth) {
        //看当下时间开始的连续时间段能不能满足容量要求，不能就遍历下一段O(t)
        //判断当前连续时间段容量是否够 O(t)
        int i = enterTime;
        for (;i<enterTime+duration;i++) {
            if (timeLine[i] < bandwidth) {
                break;
            }
        }
        if (i==enterTime+duration) return enterTime;
        //2.2处理后续时间段
        i++;
        while (i<timeLine.length) {
            int startTime = i;
            int endTime = i+duration;
            for (;i<endTime;i++) {
                if (timeLine[i] < bandwidth) {
                    break;
                }
            }
            if (i==endTime) return startTime;
            i++;
        }
        return -1;
    }

    /**
     * 绑定将要发送的流 O(t)
     */
    public void bond(Flow flow) {
        flow.setPortId(this.id);
        int startTime = flow.getSendTime();
        int duration = flow.getDuration();
        for(int i=startTime;i<startTime+duration;i++){
            timeLine[i] = timeLine[i] - flow.getBandwidth();
        }
    }

    public int getId() {
        return this.id;
    }

    public int getBandwidth() {
        return this.bandwidth;
    }
}


