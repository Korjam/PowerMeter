package com.kinwatt.powermeter.common;

public interface Function<T, R> {
    R apply(T v);
}
