package com.kinwatt.powermeter.common;

public interface Action<T> {
    void apply(T v);
}