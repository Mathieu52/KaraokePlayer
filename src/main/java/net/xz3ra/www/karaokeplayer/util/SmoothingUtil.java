package net.xz3ra.www.karaokeplayer.util;

public class SmoothingUtil {
    public static double bezier1D(double startValue, double endValue, double startSlope, double endSlope, double domain, double t) {
        double t1 = 1 - t;
        double t2 = t * t;
        double t3 = t2 * t;

        double result = Math.pow(t1, 3) * startValue +
                3 * startValue * t * Math.pow(t1, 2) +
                (startSlope * domain) * t * Math.pow(t1, 2) +
                3 * endValue * t2 * t1 -
                (endSlope * domain) * t2 * t1 +
                t3 * endValue;

        return result;
    }
}
