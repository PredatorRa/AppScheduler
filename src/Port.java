import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * 端口对象，记录出端口的带宽和占用情况
 */
public class Port {
    private final int id;
    private final int bandwidth;
    private Integer nextFreeTime;
    private final List<Map.Entry<Integer, Integer>> usage;

    public Port(int id, int bandwidth) {
        this.id = id;
        this.bandwidth = bandwidth;
        this.usage = new ArrayList<>();
        this.nextFreeTime = 0;
    }

    // 初始化端口状态
    public void init() {
        usage.clear();
        usage.add(new AbstractMap.SimpleEntry<>(0, this.bandwidth));
    }

    // 判断当前时间是否可用
    public boolean isAvailable(int currentTime) {
        for (Map.Entry<Integer, Integer> entry : this.usage) {
            if (currentTime >= entry.getKey() && currentTime < entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    // 检查是否可以发送指定带宽，更新端口使用情况
    public boolean canSend(int bandwidth) {
        for (Map.Entry<Integer, Integer> entry : this.usage) {
            int remainingBandwidth = entry.getValue() - entry.getKey();
            if (remainingBandwidth >= bandwidth) {
// 可以发送
                return true;
            }
        }
        return false;
    }
    // 发送流
    public void send(int currentTime, int bandwidth, int duration) {
        for (Map.Entry<Integer, Integer> entry : this.usage) {
            int remainingBandwidth = entry.getValue() - entry.getKey();
            if (remainingBandwidth >= bandwidth) {
                // 找到符合要求的时间段，更新使用情况
                entry.setValue(entry.getKey() + duration);
                entry.setValue(Math.min(entry.getValue(), currentTime + duration));
                while (entry.getValue() < this.usage.get(this.usage.size() - 1).getKey()) {
                    this.usage.remove(this.usage.size() - 1);
                }
                if (entry.getValue() > this.usage.get(this.usage.size() - 1).getValue()) {
                    this.usage.add(new AbstractMap.SimpleEntry<>(entry.getValue(), this.bandwidth));
                }
                break;
            }
        }
    }

    // 计算总发送时间
    public int getTotalTime() {
        int totalTime = 0;
        int lastTime = 0;
        for (Map.Entry<Integer, Integer> entry : this.usage) {
            if (entry.getKey() > lastTime) {
                totalTime += entry.getKey() - lastTime;
            }
            totalTime += entry.getValue() - entry.getKey();
            lastTime = entry.getValue();
        }
        return totalTime;
    }

    public int getId() {
        return this.id;
    }

    public int getBandwidth() {
        return this.bandwidth;
    }
}
