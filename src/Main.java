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
            Scheduler scheduler = new Scheduler(ports, flows);
            scheduler.run();
            writeFile(flows,i);
            i++;
            System.out.println(i+":"+String.valueOf(System.currentTimeMillis()-t1));
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
        return o.duration - this.duration;
    }
}

/*
 * 端口对象，记录出端口的带宽和占用情况
 */
class Port {
    private final int id;
    private final int bandwidth;
    private Set<TimePeriod> timeLine;

    public Port(int id, int bandwidth) {
        this.id = id;
        this.bandwidth = bandwidth;
        this.timeLine = new TreeSet<>();
    }

    // 初始化端口状态
    public void init() {
        timeLine.clear();
        timeLine.add(new TimePeriod(0, Integer.MAX_VALUE, bandwidth));
    }

    // 判断当前时间是否可用 O(t)
    public TimePeriod isAvailable(int startTime, int duration, int useWidth) {
        //找到当前时间点对应的时间段 O(t)
        Set<TimePeriod> periodList = findPeriodList(startTime, duration);
        //判断当前时间段容量是否够 O(t)
        for (TimePeriod timePeriod : periodList) {
            if (timePeriod.getFreeWidth() < useWidth) return null;
        }
        return periodList.iterator().next();
    }

    // 找到某个时间点对应的时间段
    private TimePeriod findPeriod(int currentTime) {
        return null;
    }

    /**
     * 找到一段时间内包含的时间段 O(t)
     * @param startTime
     * @param duration
     * @return
     */
    private Set<TimePeriod> findPeriodList(int startTime, int duration) {
        Set<TimePeriod> res = new TreeSet<>();
        for (TimePeriod timePeriod : timeLine) {
            //第一段
            if (timePeriod.getStartTime() <= startTime && timePeriod.getEndTime() > startTime) {
                res.add(timePeriod);
                continue;
            }
            //最后一段
            if(timePeriod.getStartTime()<startTime+duration&& timePeriod.getEndTime()>=startTime+duration){
                res.add(timePeriod);
                break;
            }
            //中间
            if(timePeriod.getStartTime()>=startTime&&timePeriod.getEndTime()<=startTime+duration){
                res.add(timePeriod);
            }
        }
        return res;
    }

    //找到某一个时间点的剩余带宽 O(t)
    public Integer findWidth(int time) {
        for (TimePeriod timePeriod : timeLine) {
            if (timePeriod.getStartTime() <= time && timePeriod.getEndTime() > time) {
                return timePeriod.getFreeWidth();
            }
        }
        return -1;
    }

    //找到下一段有空容量的period O(t)
    public TimePeriod findNextAvailablePeriod(int enterTime, int duration, int bandwidth) {
        //1.找到第一个开始的timePeriod O(t)
        Iterator<TimePeriod> iterator = timeLine.iterator();
        TimePeriod timePeriod = null;
        while (iterator.hasNext()) {
            timePeriod = iterator.next();
            if (timePeriod.getEndTime() > enterTime) break;
        }
        //2.看当下时间开始的连续时间段能不能满足容量要求，不能就遍历下一段O(t)
        boolean isAvailable = true;
        TimePeriod res = timePeriod;
        //2.1处理当下时间段
        //找到当前时间点对应的时间段 O(t)
        Set<TimePeriod> periodList = findPeriodList(enterTime, duration);
        //判断当前连续时间段容量是否够 O(t)
        for (TimePeriod t : periodList) {
            if (t.getFreeWidth() < bandwidth){
                isAvailable=false;
                break;
            }
            //保持timePeriod和t同步,t是第一个不满足带宽条件的时间段
            if(iterator.hasNext())timePeriod = iterator.next();
        }
        if (isAvailable) return res;
        //2.2处理后续时间段
        while (iterator.hasNext()) {
            timePeriod = iterator.next();
            res = timePeriod;
            periodList = findPeriodList(timePeriod.getStartTime(), duration);
            isAvailable = true;
            for (TimePeriod t : periodList) {
                if (t.getFreeWidth() < bandwidth){
                    isAvailable=false;
                    break;
                }
                //保持timePeriod和t同步,t是第一个不满足带宽条件的时间段
                if(iterator.hasNext())timePeriod = iterator.next();
            }
            if (isAvailable) return res;
        }
        return null;
    }

    /**
     * 绑定将要发送的流 O(t)
     */
    public void bond(Flow flow) {
        flow.setPortId(this.id);
        //当前时间enterTime发送
        if (flow.getSendTime() == -1) {
            flow.setSendTime(flow.getEnterTime());
            Set<TimePeriod> periodList = findPeriodList(flow.getEnterTime(), flow.getDuration());
            send(periodList, flow.getSendTime(), flow.getDuration(), flow.getBandwidth());
            return;
        }
        //不是当前时间发送
        Set<TimePeriod> periodList = findPeriodList(flow.getSendTime(), flow.getDuration());
        send(periodList, flow.getSendTime(), flow.getDuration(), flow.getBandwidth());
    }

    /**
     * 根据发送的流的时间转换时间轴
     *
     * @param periodList
     * @param sendTime
     * @param duration
     */
    private void send(Set<TimePeriod> periodList, int sendTime, int duration, int bandwidth) {
        //只有一段
        if (periodList.size() == 1) {
            TimePeriod timePeriod = periodList.iterator().next();
            timeLine.remove(timePeriod);
            TimePeriod t1 = new TimePeriod(timePeriod.getStartTime(), sendTime, timePeriod.getFreeWidth());
            TimePeriod t2 = new TimePeriod(sendTime, sendTime + duration, timePeriod.getFreeWidth() - bandwidth);
            TimePeriod t3 = new TimePeriod(sendTime + duration, timePeriod.getEndTime(), timePeriod.getFreeWidth());
            if(t1.getStartTime()!=t1.getEndTime())addTimePeriod(t1);
            if(t2.getStartTime()!=t2.getEndTime())addTimePeriod(t2);
            if(t3.getStartTime()!=t3.getEndTime())addTimePeriod(t3);
        }
        //多段
        periodList.stream().forEach(item -> {
            //第一段
            if (item.getStartTime() < sendTime && item.getEndTime() > sendTime) {
                timeLine.remove(item);
                addTimePeriod(new TimePeriod(item.getStartTime(),sendTime,item.getFreeWidth()));
                addTimePeriod(new TimePeriod(sendTime,item.getEndTime(),item.getFreeWidth()-bandwidth));
                return;
            }
            //最后一段
            if(item.getStartTime() < sendTime+duration && item.getEndTime() > sendTime+duration){
                timeLine.remove(item);
                addTimePeriod(new TimePeriod(item.getStartTime(),sendTime+duration,item.getFreeWidth()-bandwidth));
                addTimePeriod(new TimePeriod(sendTime+duration,item.getEndTime(),item.getFreeWidth()));
                return;
            }
            //中间段
            item.setFreeWidth(item.getFreeWidth()-bandwidth);
        });
    }

    private void addTimePeriod(TimePeriod period){
        if(period.getStartTime()>=period.getEndTime())return;
        timeLine.add(period);
    }

    public int getId() {
        return this.id;
    }

    public int getBandwidth() {
        return this.bandwidth;
    }

    public Set<TimePeriod> getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(Set<TimePeriod> timeLine) {
        this.timeLine = timeLine;
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
            sendFlow(port,flow);
        }
    }

    /**
     * 选择合适的端口 O(pt)
     * @param flow
     * @return
     */
    private Port choosePort(Flow flow) {
        //1.选出在flow开始时间有容量的第一个端口 O(pt)
        for (Port port : ports) {
            TimePeriod period = port.isAvailable(flow.getEnterTime(), flow.getDuration(), flow.getBandwidth());
            if(period!=null){
                return port;
            }
        }
        //2.如果当前时间没有可以传输的节点，则顺延到随机的有空余容量的端口 O(pt)
        int i = new Random().nextInt(ports.size());
        Port port = ports.get(i);
        TimePeriod period = port.findNextAvailablePeriod(flow.getEnterTime(),flow.getDuration(),flow.getBandwidth());
        flow.setSendTime(period.getStartTime());
        return port;
    }

    /**
     * 尝试发送一个流，更新端口状态和流状态 O(t)
     * @param port
     * @param flow
     */
    private void sendFlow(Port port, Flow flow) {
        //端口绑定流 O(t)
        port.bond(flow);
    }


}

class TimePeriod implements Comparable<TimePeriod>{
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
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(TimePeriod o) {
        return this.startTime-o.startTime;
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

