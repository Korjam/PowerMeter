package com.kinwatt.powermeter.ui;

public interface ActivityView {
    void setSpeed(float speed);
    void setAltitude(double altitude);
    void setDistance(float distance);
    void setPowerAverage3(float power);
    void setPowerAverage5(float power);
    void setPowerAverage10(float power);
}
