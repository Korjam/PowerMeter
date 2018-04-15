package com.kinwatt.powermeter.model;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.User;

public class CyclingIndoorPowerAlgorithm extends PowerAlgorithm {

    private static final float cRolling = 5.97f;
    private static final float CdA = 0.179f;
    private static final double kinMass =  3.5f;

    public CyclingIndoorPowerAlgorithm(User user) {
        super(user);
    }

    @Override
    public float calculatePower(Location pos1, Location pos2) {
        return calculatePower(pos1, pos2, 0);
    }

    @Override
    public float calculatePower(Location pos1, Location pos2, double grade) {
        float avgSpeed = MathUtils.average(pos1.getSpeed(), pos2.getSpeed());
        double Pkin = kinMass * (Math.pow(pos2.getSpeed(), 2) - Math.pow(pos1.getSpeed(), 2));
        double Pr = cRolling * avgSpeed;
        double Pd = CdA * Math.pow(avgSpeed, 3);
        return (int)Math.max(0, Pkin + Pr + Pd);
    }
}
