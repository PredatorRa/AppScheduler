public class TimePeriod implements Comparable<TimePeriod>{
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
}
