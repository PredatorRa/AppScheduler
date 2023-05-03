import java.time.Period;import java.util.*;/* * 端口对象，记录出端口的带宽和占用情况 */public class Port {    private final int id;    private final int bandwidth;    private Set<TimePeriod> timeLine;    public Port(int id, int bandwidth) {        this.id = id;        this.bandwidth = bandwidth;        this.timeLine = new HashSet<>();    }    // 初始化端口状态    public void init() {        timeLine.clear();        timeLine.add(new TimePeriod(0, Integer.MAX_VALUE, bandwidth));    }    // 判断当前时间是否可用    public boolean isAvailable(int startTime, int duration, int useWidth) {        //找到当前时间点对应的时间段        List<TimePeriod> periodList = findPeriodList(startTime, duration);        //判断当前时间段容量是否够        for (TimePeriod timePeriod : periodList) {            if (timePeriod.getFreeWidth() < useWidth) return false;        }        return true;    }    // 找到某个时间点对应的时间段    private TimePeriod findPeriod(int currentTime) {        return null;    }    /**     * 找到一段时间内包含的时间段     *     * @param startTime     * @param duration     * @return     */    private List<TimePeriod> findPeriodList(int startTime, int duration) {        List<TimePeriod> res = new ArrayList<>();        for (TimePeriod timePeriod : timeLine) {            if (timePeriod.getStartTime() <= startTime || timePeriod.getEndTime() >= startTime + duration) {                res.add(timePeriod);            }        }        return res;    }    //找到某一个时间点的剩余带宽    public Integer findWidth(int time) {        for (TimePeriod timePeriod : timeLine) {            if (timePeriod.getStartTime() <= time && timePeriod.getEndTime() >= time) {                return timePeriod.getFreeWidth();            }        }        return -1;    }    //找到下一段有空容量的period    public TimePeriod findNextAvailablePeriod(int enterTime, int duration, int bandwidth) {        //排序timePeriod        Collections.sort(timeLine);        //找到第一个开始的timePeriod        Iterator<TimePeriod> iterator = timeLine.iterator();        TimePeriod timePeriod = null;        while (iterator.hasNext()) {            timePeriod = iterator.next();            if (timePeriod.getEndTime() >= enterTime) break;        }        //看当下时间开始的连续时间段能不能满足容量要求        if (isAvailable(enterTime, duration, bandwidth)) return timePeriod;        while (iterator.hasNext()) {            timePeriod = iterator.next();            if (isAvailable(timePeriod.getStartTime(), duration, bandwidth)) return timePeriod;        }        return null;    }    /**     * 绑定将要发送的流     */    public void bond(Flow flow) {        flow.setPortId(this.id);        //当前时间enterTime发送        if (flow.getSendTime() == -1) {            flow.setSendTime(flow.getEnterTime());            List<TimePeriod> periodList = findPeriodList(flow.getEnterTime(), flow.getDuration());            send(periodList, flow.getSendTime(), flow.getDuration(), flow.getBandwidth());            return;        }        //不是当前时间发送        List<TimePeriod> periodList = findPeriodList(flow.getSendTime(), flow.getDuration());        send(periodList, flow.getSendTime(), flow.getDuration(), flow.getBandwidth());    }    /**     * 根据发送的流的时间转换时间轴     *     * @param periodList     * @param sendTime     * @param duration     */    private void send(List<TimePeriod> periodList, int sendTime, int duration, int bandwidth) {        //只有一段        if (periodList.size() == 1) {            TimePeriod timePeriod = periodList.get(0);            timeLine.remove(timePeriod);            TimePeriod t1 = new TimePeriod(timePeriod.getStartTime(), sendTime, timePeriod.getFreeWidth() - bandwidth);            TimePeriod t2 = new TimePeriod(sendTime, sendTime + duration, timePeriod.getFreeWidth() - bandwidth);            TimePeriod t3 = new TimePeriod(sendTime + duration, timePeriod.getEndTime(), timePeriod.getFreeWidth() - bandwidth);            if(t1.getStartTime()!=t1.getEndTime())timeLine.add(t1);            if(t2.getStartTime()!=t2.getEndTime())timeLine.add(t2);            if(t3.getStartTime()!=t3.getEndTime())timeLine.add(t3);        }        //多段        Collections.sort(periodList);        periodList.stream().forEach(item -> {            //第一段            if (item.getStartTime() < sendTime && item.getEndTime() > sendTime) {                timeLine.remove(item);                timeLine.add(new TimePeriod(item.getStartTime(),sendTime,item.getFreeWidth()-bandwidth));                timeLine.add(new TimePeriod(sendTime,item.getEndTime(),item.getFreeWidth()-bandwidth));            }            //最后一段            if(item.getStartTime() < sendTime+duration && item.getEndTime() > sendTime+duration){                timeLine.remove(item);                timeLine.add(new TimePeriod(item.getStartTime(),sendTime+duration,item.getFreeWidth()-bandwidth));                timeLine.add(new TimePeriod(sendTime+duration,item.getEndTime(),item.getFreeWidth()-bandwidth));            }            item.setFreeWidth(item.getFreeWidth()-bandwidth);        });    }    public int getId() {        return this.id;    }    public int getBandwidth() {        return this.bandwidth;    }    public List<TimePeriod> getTimeLine() {        return timeLine;    }    public void setTimeLine(List<TimePeriod> timeLine) {        this.timeLine = timeLine;    }}