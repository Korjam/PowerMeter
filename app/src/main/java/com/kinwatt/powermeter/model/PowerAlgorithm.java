package com.kinwatt.powermeter.model;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.User;

public abstract class PowerAlgorithm {

    //For now I'm setting the user's parameters here until we define the class User.
    protected static final float totalMass = 80;
    protected static final float gForce = 9.80665f;
    protected static final float cRolling = 0.004f;
    protected static final float CdA = 0.32f;
    protected static final float rho = 1.226f;
    protected static final float drag = CdA * rho / 2;

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
