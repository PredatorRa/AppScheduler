import java.util.ArrayList;
import java.util.List;

/*

读取端口文件和流文件，并创建 Scheduler 对象进行调度
*/
public class Main {
    public static void main(String[] args) {
// 读取端口文件和流文件
        List<Port> ports = new ArrayList<>();
        ports.add(new Port(1, 10));
        ports.add(new Port(2, 20));
        ports.add(new Port(3, 30));

        List<Flow> flows = new ArrayList<>();
        flows.add(new Flow(1, 5, 0, 5));
        flows.add(new Flow(2, 10, 0, 10));
        flows.add(new Flow(3, 15, 0, 15));

        // 创建 Scheduler 对象进行调度
        Scheduler scheduler = new Scheduler(ports, flows);
        scheduler.run();
    }
}
