import java.util.Objects;

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
