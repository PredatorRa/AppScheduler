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
        // 按照进入设备时间升序排序
        Collections.sort(this.flows);

        // 初始化端口状态
        for (Port port : this.ports) {
            port.init();
        }

        // 逐个发送待发送的流，直到全部发送完成
        for (Flow flow : flows) {
            //找到第一个空闲的端口
            Port firstFreePort = findFirstFreePort(ports);
            //发送当前流
            sendFlow(flow,firstFreePort);
        }

        // 计算总发送时间
        int totalTime = 0;
        for (Port port : this.ports) {
            totalTime = Math.max(totalTime, port.getTotalTime());
        }

        System.out.println("Total time: " + totalTime);
    }

    private Port findFirstFreePort(List<Port> ports) {
        return null;
    }

    /*
     * 尝试发送一个流，如果成功则更新端口状态，返回 true；否则返回 false。
     */
    private boolean sendFlow(Flow flow,Port port) {

        return true;
    }


}