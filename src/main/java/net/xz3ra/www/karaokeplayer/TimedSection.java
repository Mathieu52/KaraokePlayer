package net.xz3ra.www.karaokeplayer;

public class TimedSection implements Cloneable {
    private float startTime;
    private float endTime;
    private String value;

    public TimedSection(float startTime, float endTime, String value) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.value = value;
    }

    public void setStartTime(float time) {
        startTime = time;
    }

    public void setEndTime(float time) {
        endTime = time;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public String getValue() {
        return value;
    }

    public float calculateSlope() {
        return (float) value.length() / (endTime - startTime);
    }

    @Override
    public String toString() {
        return "TimedSection{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public TimedSection clone() {
        try {
            TimedSection cloned = (TimedSection) super.clone();
            // Create a new String object to avoid referencing the same value
            cloned.value = new String(this.value);
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            // This should never happen since TimedSection implements Cloneable
            throw new InternalError(e);
        }
    }
}