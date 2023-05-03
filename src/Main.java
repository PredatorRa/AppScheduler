import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*

读取端口文件和流文件，并创建 Scheduler 对象进行调度
*/
public class Main {
    public static void main(String[] args) {
        // 读取端口文件和流文件
        int i=0;
        List<Port> ports = null;
        List<Flow> flows = null;
        while(true) {
            ports = readPorts(String.format("./data/%d/port.txt",i ));
            flows = readFlows(String.format("./data/%d/flow.txt",i ));
            if(ports==null||ports.isEmpty()){
                break;
            }
            // 创建 Scheduler 对象进行调度
            Scheduler scheduler = new Scheduler(ports, flows);
            scheduler.run();
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return portList;
    }
}
