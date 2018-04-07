package com.kinwatt.powermeter.model;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.User;

public abstract class PowerAlgorithm {

    private User user;

    public PowerAlgorithm(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public abstract float calculatePower(Location pos1, Location pos2);

    public abstract float calculatePower(Location pos1, Location pos2, double grade);
}
