import java.io.*;
import java.sql.Time;
import java.util.*;

/*

读取端口文件和流文件，并创建 Scheduler 对象进行调度
*/
public class Main {
    public static void main(String[] args) {
        // 读取端口文件和流文件
        int i = 0;
        List<Port> ports = null;
        List<Flow> flows = null;
        while (true) {
            long startTime = System.currentTimeMillis();
            ports = readPorts(String.format("../data/%d/port.txt", i));
            flows = readFlows(String.format("../data/%d/flow.txt", i));
            if (ports == null || ports.isEmpty()) {
                break;
            }
            // 创建 Scheduler 对象进行调度
            Scheduler scheduler = new Scheduler(ports, flows);
            scheduler.run();
            writeFile(flows, i);
            System.out.println(i+":"+(System.currentTimeMillis()-startTime));
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
    private static void writeFile(List<Flow> flows, int i) {
        try {
            File outputFile = new File(String.format("../data/%d/result.txt", i));
            PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, false));
            for (Flow flow : flows) {
                writer.println(flow.getId() + "," + flow.getPortId() + "," + flow.getSendTime());
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
        return o.duration - this.duration;
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

class Scheduler {
    // 端口列表
    List<Port> ports;
    // 流列表
    List<Flow> flows;

    public Scheduler(List<Port> ports, List<Flow> flows) {
        this.ports = ports;
        this.flows = flows;
    }

    public void run() {
        // 按照时间跨度降序排序 O(f)
        Collections.sort(this.flows);

        // 初始化端口状态 O(p)
        for (Port port : this.ports) {
            port.init();
        }

        // 逐个发送待发送的流，直到全部发送完成 O(fpt)
        Iterator<Flow> iter = this.flows.iterator();
        while (iter.hasNext()) {
            Flow flow = iter.next();
            //判断将当前flow发送到哪一个Port比较合适 O(pt)
            Port port = choosePort(flow);
            //发送流 O(t)
            sendFlow(port, flow);
        }
    }

    /**
     * 选择合适的端口 O(pt)
     *
     * @param flow
     * @return
     */
    private Port choosePort(Flow flow) {
        Port choosedPort = null;
        int minWidth = Integer.MAX_VALUE;
        //1.选出在flow开始时间有容量的且当前容量最小的端口 O(pt)
        for (Port port : ports) {
            int width = port.isAvailable(flow.getEnterTime(), flow.getDuration(), flow.getBandwidth());
            if (width!=-1 && width< minWidth) {
                choosedPort = port;
                minWidth = width;
            }
        }
        if (choosedPort != null) {
            flow.setSendTime(flow.getEnterTime());
            return choosedPort;
        }
        //2.如果当前时间没有可以传输的节点，则顺延到距离最近的有空余容量的端口 O(pt)
        Port nearestPort = null;
        Integer nearestTime = Integer.MAX_VALUE;
        for (Port port : ports) {
            int startTime = port.findNextAvailablePeriod(flow.getEnterTime(), flow.getDuration(), flow.getBandwidth());
            if (startTime < nearestTime) {
                nearestTime = startTime;
                nearestPort = port;
            }
        }
        flow.setSendTime(nearestTime);
        return nearestPort;
    }

    /**
     * 尝试发送一个流，更新端口状态和流状态 O(t)
     *
     * @param port
     * @param flow
     */
    private void sendFlow(Port port, Flow flow) {
        //端口绑定流 O(t)
        port.bond(flow);
    }


}

class TimePeriod implements Comparable<TimePeriod> {
    private Integer startTime;
    private Integer endTime;
    private Integer freeWidth;

    public TimePeriod(Integer startTime, Integer endTime, Integer freeWidth) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.freeWidth = freeWidth;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    public Integer getFreeWidth() {
        return freeWidth;
    }

    public void setFreeWidth(Integer freeWidth) {
        this.freeWidth = freeWidth;
    }

    /**
     * 按照开始时间升序排列
     *
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(TimePeriod o) {
        return this.startTime - o.startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod that = (TimePeriod) o;
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}

