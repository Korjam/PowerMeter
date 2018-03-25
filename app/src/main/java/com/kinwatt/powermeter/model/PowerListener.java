package com.kinwatt.powermeter.model;

public interface PowerListener {
    void onPowerMeasured(long time, float power);
}
