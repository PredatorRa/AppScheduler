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
        // 存放待发送的流
        PriorityQueue<Flow> waitingList = new PriorityQueue<>(this.flows.size(), new FlowComparator());

        // 按照进入设备时间升序排序
        Collections.sort(this.flows);

        // 初始化端口状态
        for (Port port : this.ports) {
            port.init();
        }

        // 当前时间
        int currentTime = 0;

        // 逐个发送待发送的流，直到全部发送完成
        while (!waitingList.isEmpty() || !this.flows.isEmpty()) {
            // 根据当前时间找到可用的出端口
            Set<Integer> availablePorts = new HashSet<>();
            for (Port port : this.ports) {
                if (port.isAvailable(currentTime)) {
                    availablePorts.add(port.getId());
                }
            }

            // 开始发送待发送的流
            Iterator<Flow> iter = this.flows.iterator();
            while (iter.hasNext()) {
                Flow flow = iter.next();
                //todo 判断将当前flow发送到哪一个Port比较合适
                if (!availablePorts.isEmpty()) {
                    // 找到了可用的出端口，尝试进行发送
                    Integer next = availablePorts.iterator().next();
                    flow.setPortId(next);
                    boolean sent = this.sendFlow(currentTime, flow);
                    if (sent) {
                        iter.remove();
                    } else {
                        // 发送失败，加入排队列表
                        waitingList.offer(flow);
                    }
                } else {
                    // 出端口不可用，加入排队列表
                    waitingList.offer(flow);
                }
            }

            // 检查排队列表中是否有超时的流，超时时间为10秒
            while (!waitingList.isEmpty()) {
                Flow flow = waitingList.peek();
                if (currentTime - flow.getEnterTime() >= 10) {
                    waitingList.poll();
                } else {
                    break;
                }
            }

            // 更新当前时间
            currentTime += 1; // 时间步长为1秒
        }

        // 计算总发送时间
        int totalTime = 0;
        for (Port port : this.ports) {
            totalTime = Math.max(totalTime, port.getTotalTime());
        }

        System.out.println("Total time: " + totalTime);
    }

    /*
     * 尝试发送一个流，如果成功则更新端口状态，返回 true；否则返回 false。
     */
    private boolean sendFlow(int currentTime, Flow flow) {
        Port targetPort = null;
        for (Port port : this.ports) {
            if (port.getId() == flow.getPortId()) {
                targetPort = port;
                break;
            }
        }

        if (targetPort == null || !targetPort.canSend(flow.getBandwidth())) {
            // 出端口不存在或者带宽不足，发送失败
            return false;
        }

        // 发送流成功，更新端口状态
        targetPort.send(currentTime, flow.getBandwidth(), flow.getDuration());
        return true;
    }

    /*
     * 流对象的比较器，用于让 PriorityQueue 按照剩余发送时间升序排序
     */
    private static class FlowComparator implements Comparator<Flow> {
        @Override
        public int compare(Flow o1, Flow o2) {
            return o1.getEnterTime() + o1.getDuration() - o2.getEnterTime() - o2.getDuration();
        }
    }
}