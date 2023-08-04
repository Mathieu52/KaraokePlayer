package net.xz3ra.www.karaokeplayer.util;

public class RelativeUITools {
    public static double calculate(double size, double initialSize, double realSize) {
        return size * realSize / initialSize;
    }

    public static double calculate(double size, double initialWidth, double realWidth, double initialHeight, double realHeight) {
        double ratio = Math.min(realWidth / initialWidth, realHeight / initialHeight);
        return size * ratio;
    }
}