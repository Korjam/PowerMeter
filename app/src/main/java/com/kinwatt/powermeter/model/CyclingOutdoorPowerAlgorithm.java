package com.kinwatt.powermeter.model;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.Bike;
import com.kinwatt.powermeter.data.BikeType;
import com.kinwatt.powermeter.data.User;

public class CyclingOutdoorPowerAlgorithm extends PowerAlgorithm {

    //TODO: Let the user select a bike type and use it here
    //TODO: Let the user select a tyre for his bike so cRolling does not depend on the bike
    //TODO: Make rho not final... At least to make it a a function of altitude
    private static final float gForce = 9.80665f;
    private static final float rho = 1.226f;
    private float cRolling;
    private float CdA;
    private float totalMass;
    private float drag = CdA * rho / 2;

    public CyclingOutdoorPowerAlgorithm(User user) {
        super(user);
        if (user != null) {
            Bike currentBike = user.getBikes().get(0);
            totalMass = user.getWeight() + currentBike.getWeight();
            
            float percent = 1;
            switch (currentBike.getType()){
                case Road:
                    percent = 0.8f;
                    cRolling = 0.004f;
                    break;
                case Mountain:
                    percent = 1f;
                    cRolling = 0.008f;
                    break;
            }
            CdA = (float) (percent * (0.0285 * Math.pow(user.getHeight() / 100, 0.725) *
                                               Math.pow(user.getWeight(), 0.425) + 0.17));
        }
        else {
            totalMass = 80;
            CdA=0.36f;
            cRolling = 0.005f;
        }
    }

    @Override
    public float calculatePower(Location pos1, Location pos2) {
        //Calculating increments
        // It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.
        double hDiff = pos2.getAltitude() - pos1.getAltitude();
        double grade = hDiff / pos1.distanceTo(pos2);
        return calculatePower(pos1, pos2, grade);
    }

    @Override
    public float calculatePower(Location pos1, Location pos2, double grade) {
        double beta = Math.atan(grade);

        //Calculate power from p1.getSeconds()... p2.getSeconds()-1;
        double avgSpeed = MathUtils.average(pos2.getSpeed(), pos1.getSpeed());
        double pKin = (Math.pow(pos2.getSpeed(), 2) - Math.pow(pos1.getSpeed(), 2)) * totalMass / 2;
        double pGravity = avgSpeed * gForce * totalMass * Math.sin(beta);
        double pDrag = Math.pow(avgSpeed, 3) * drag;
        double pRolling = avgSpeed * cRolling * gForce * totalMass * Math.cos(beta);
        double power = pKin + pGravity + pDrag + pRolling;

        power = Math.max(0, power);
        return (float) (Double.isNaN(power) ? 0 : power);
    }

    public float calculateSpeed(float power, float speed0, double grade){
        double beta = Math.atan(grade);
        double pGravity = speed0 * gForce * totalMass * Math.sin(beta);
        double pDrag = Math.pow(speed0, 3) * drag;
        double pRolling = speed0 * cRolling * gForce * totalMass * Math.cos(beta);
        double finalPower = power - (pDrag + pGravity + pRolling);
        double result = (finalPower + 0.5 * totalMass * Math.pow(speed0,2)) / (0.5 * totalMass);
        return (float) Math.sqrt(Math.max(0, result));
    }
}
