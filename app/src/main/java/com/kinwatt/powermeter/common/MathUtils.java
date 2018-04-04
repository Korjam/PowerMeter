package com.kinwatt.powermeter.common;

import java.util.Collection;
import java.util.List;

public final class MathUtils {

    public static float average(Collection<Float> items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.size();
    }

    public static float average(float... items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.length;
    }

    public static float angularVelocity(float rpm) {
        return (float)(2 * Math.PI * rpm / 60);
    }

    public static float linealVelocity(float rpm, float radious) {
        return radious * angularVelocity(rpm);
    }

    /**
     * Gives the best average @seg seconds for powerList
     *
     * @param powerList List of activity power, second by second
     * @param seg The number of seconds to get the best average of
     */
    public static int cpseg(List<Integer> powerList, int seg) {
        int sum = 0, bestSum = 0;
        for (int i = 0; i < seg; i++) {
            sum += powerList.get(i);
        }
        bestSum = sum;
        for (int i = seg; i < powerList.size(); i++) {
            sum += powerList.get(i) - powerList.get(i-seg);
            if (sum > bestSum) {
                bestSum = sum;
            }
        }
        return Math.round((float)bestSum / seg);
    }

    public static <T extends Number, V extends Number> V interpolate(T x1, V y1, T x2, V y2, T v) {
        double _x1 = x1.doubleValue();
        double _y1 = y1.doubleValue();
        double _x2 = x2.doubleValue();
        double _y2 = y2.doubleValue();

        if (_x1 >= _x2) throw new IllegalArgumentException();

        final double m = (_y2 - _y1) / (_x2 - _x1);
        final double n = _y1 - m * _x1;

        Number res = m * v.doubleValue() + n;

        if (y1 instanceof Double) {
            return (V)(Number)res.doubleValue();
        }
        else if (y1 instanceof Float) {
            return (V)(Number)res.floatValue();
        }
        else if (y1 instanceof Long) {
            return (V)(Number)res.longValue();
        }
        else if (y1 instanceof Integer) {
            return (V)(Number)res.intValue();
        }
        else {
            throw  new RuntimeException();
        }
    }

    public static <T extends Number, V extends Number> Function<T, V> interpolate(T x1, V y1, T x2, V y2) {
        double _x1 = x1.doubleValue();
        double _y1 = y1.doubleValue();
        double _x2 = x2.doubleValue();
        double _y2 = y2.doubleValue();

        if (_x1 >= _x2) throw new IllegalArgumentException();

        final double m = (_y2 - _y1) / (_x2 - _x1);
        final double n = _y1 - m * _x1;

        if (y1 instanceof Double) {
            return v -> {
                Number res = m * v.doubleValue() + n;
                return (V)(Number)res.doubleValue();
            };
        }
        else if (y1 instanceof Float) {
            return v -> {
                Number res = m * v.doubleValue() + n;
                return (V)(Number)res.floatValue();
            };
        }
        else if (y1 instanceof Long) {
            return v -> {
                Number res = m * v.doubleValue() + n;
                return (V)(Number)res.longValue();
            };
        }
        else if (y1 instanceof Integer) {
            return v -> {
                Number res = m * v.doubleValue() + n;
                return (V)(Number)res.intValue();
            };
        }
        else {
            throw new RuntimeException("This exception never should be thrown.");
        }
    }
}