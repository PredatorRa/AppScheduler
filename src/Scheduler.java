import java.time.Period;
import java.util.*;


public class Scheduler {
    // 端口列表
    List<Port> ports;
    // 流列表
    List<Flow> flows;

    public Scheduler(List<Port> ports, List<Flow> flows) {
        this.ports = ports;
        this.flows = flows;
    }

    public void run() {
        // 按照时间跨度降序排序
        Collections.sort(this.flows);

        // 初始化端口状态
        for (Port port : this.ports) {
            port.init();
        }

        // 逐个发送待发送的流，直到全部发送完成
        Iterator<Flow> iter = this.flows.iterator();
        while (iter.hasNext()) {
            Flow flow = iter.next();
            //判断将当前flow发送到哪一个Port比较合适
            Port port = choosePort(flow);
            //发送流
            sendFlow(port,flow);
        }

        //输出结果
        writeFile();
    }

    /**
     * 选择合适的端口
     * @param flow
     * @return
     */
    private Port choosePort(Flow flow) {
        List<Port> availablePorts = new ArrayList<>();
        //选出在flow开始时间有容量的端口
        for (Port port : ports) {
            if(port.isAvailable(flow.getEnterTime(),flow.getDuration(),flow.getBandwidth())){
                availablePorts.add(port);
            }
        }
        //选出可以传输流的端口里容量最小的
        if(!availablePorts.isEmpty()){
            Port port = availablePorts.stream().reduce((x, y) -> x.findWidth(flow.getEnterTime()) < y.findWidth(flow.getEnterTime()) ? x : y).get();
            return port;
        }
        //如果当前时间没有可以传输的节点，则顺延到距离最近的有空余容量的端口
        Port nearestPort = null;
        Integer nearestTime = Integer.MAX_VALUE;
        for (Port port : ports) {
            TimePeriod period = port.findNextAvailablePeriod(flow.getEnterTime(),flow.getDuration(),flow.getBandwidth());
            if(period.getStartTime()<nearestTime){
                nearestTime = period.getStartTime();
                nearestPort = port;
            }
        }
        flow.setSendTime(nearestTime);
        return nearestPort;
    }

    /**
     * 尝试发送一个流，更新端口状态和流状态
     * @param port
     * @param flow
     */
    private void sendFlow(Port port, Flow flow) {
        //端口绑定流
        port.bond(flow);
    }

    //把结果写入文件
    private void writeFile() {
    }


}