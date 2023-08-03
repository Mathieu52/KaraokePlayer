package net.xz3ra.www.karaokeplayer.manager;

import java.io.Serializable;

public class TimeManager implements Serializable {
    private long perceived_time;
    private long temp_time = System.currentTimeMillis();
    private long delta_time;
    private double scale;

    /**
     * Default constructor of TimeManager
     */
    public TimeManager() {
        this(0, 1);
    }

    /**
     * Constructor of TimeManager with parameter for time scale
     *
     * @param scale The scale at which time flows.
     */
    public TimeManager(double scale) {
        this(0, scale);
    }

    public TimeManager(long startTime, double scale) {
        this.perceived_time = startTime;
        this.scale = scale;
    }

    public TimeManager(long startTime) {
        this(startTime, 1);
    }

    /**
     * Updates this instance of TimeManager (delta time & perceived time)
     */
    public void update() {
        long time = System.currentTimeMillis();
        delta_time = time - temp_time;
        temp_time = time;

        perceived_time += currentDeltaTimeMillis();
    }

    /**
     * Returns the time in milliseconds
     *
     * @return Long, time in milliseconds
     */
    public long currentTimeMillis() {
        return perceived_time;
    }

    /**
     * Returns the time in seconds
     *
     * @return Double, time in seconds
     */
    public double currentTime() {
        return currentTimeMillis();
    }

    /**
     * Returns the delta time in milliseconds
     *
     * @return Double, delta time in milliseconds
     */
    public double currentDeltaTimeMillis() {
        return delta_time * scale;
    }

    /**
     * Returns the delta time in seconds
     *
     * @return Double, delta time in milliseconds
     */
    public double currentDeltaTime() {
        return currentDeltaTimeMillis() * 0.001;
    }

    /**
     * Returns the time scale
     *
     * @return Double, the scale at which time flows.
     */
    public double getTimeScale() {
        return scale;
    }

    /**
     * Sets the time scale
     */
    public void setTimeScale(double scale) {
        this.scale = scale;
    }
}