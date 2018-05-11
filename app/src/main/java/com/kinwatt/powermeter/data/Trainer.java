package com.kinwatt.powermeter.data;

import com.kinwatt.powermeter.common.Collections;
import com.kinwatt.powermeter.common.Iterables;
import com.kinwatt.powermeter.common.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class Trainer {

    private String make;
    private String model;
    private double cRolling;
    private double CdA;
    private double kinMass;

    public Trainer(){}

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getcRolling() {
        return cRolling;
    }

    public void setcRolling(double cRolling) {
        this.cRolling = cRolling;
    }

    public double getCdA() {
        return CdA;
    }

    public void setCdA(double cdA) {
        CdA = cdA;
    }

    public double getKinMass() {
        return kinMass;
    }

    public void setKinMass(double kinMass) {
        this.kinMass = kinMass;
    }

    /**
     * Calculates the parameters of the trainer calibration.
     * @param speed List of speeds in m/s.
     * @param power List of power in W.
     */
    public void calibrateTrainer(List<Float> speed, List<Integer> power) {

        //Copy speed (in m/s) and power in new lists (we are going to remove some data from them)
        List<Float> speedCopy = Iterables.toList(speed);
        List<Integer> powerCopy = Iterables.toList(power);

        //remove pairs (speed,power) in which power=0
        for (int i = power.size() - 1; i >= 0; i--) {
            if (power.get(i) == 0) {
                power.remove(i);
                speed.remove(i);
            }
        }

        //Matrix X
        double[][] matrixX = new double[2][speed.size()];
        for (int i = 0; i < speed.size(); i++) {
            matrixX[0][i] = speed.get(i);
            matrixX[1][i] = (float) Math.pow(speed.get(i), 3);
        }

        //Matrix X'X
        double[][] XtX = new double[2][2];
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 2; j++){
                for (int k = 0; k < speed.size(); k++){
                    XtX[i][j] += matrixX[i][k] * matrixX[j][k];
                }
            }
        }

        //Inverse matrix calculation
        //a=XtX[0][0];b=XtX[0][1]=c=XtX[1][0];d=XtX[1][1]
        double det = XtX[0][0] * XtX[1][1] - XtX[0][1] * XtX[0][1];
        double[][] invXtX = new double[2][2];
        invXtX[0][0] = XtX[1][1] / det;
        invXtX[0][1] = -XtX[0][1] / det;
        invXtX[1][0] = invXtX[0][1];
        invXtX[1][1] = XtX[0][0] / det;

        //Calculating matrix inv(XtX)Xt (Moore–Penrose pseudoinverse)
        double[][] pseudoinverse = new double[2][speed.size()];
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < speed.size(); j++){
                pseudoinverse[i][j] = invXtX[i][0] * matrixX[0][j] + invXtX[i][1] * matrixX[1][j];
            }
        }

        //Obtaining Crr and CdA finally.
        double Crr = 0, CdA = 0;
        for (int i = 0; i < speed.size(); i++){
            Crr += pseudoinverse[0][i] * power.get(i);
            CdA += pseudoinverse[1][i] * power.get(i);
        }
        this.cRolling = Crr;
        this.CdA = CdA;

        //Calculate estimates of kinMass for suitable candidates
        List<Double> kinMass = new ArrayList<>();
        for (int i = 1; i < powerCopy.size(); i++) {
            if (powerCopy.get(i) == 0 &&
                    powerCopy.get(i - 1) == 0 &&
                    speedCopy.get(i) > 0 &&
                    speedCopy.get(i).intValue() != speedCopy.get(i - 1).intValue()){
                kinMass.add((Crr * speedCopy.get(i) + CdA * Math.pow(speedCopy.get(i), 3)) /
                        (Math.pow(speedCopy.get(i - 1), 2) - Math.pow(speedCopy.get(i), 2)));
            }
        }

        //Remove kinMass elements such that kinMass < 0
        Collections.remove(kinMass, v -> v < 0);

        //Calculate mean of kinMass
        double meanKinMass = MathUtils.averageDouble(kinMass);

        //Calculate standard deviation
        double stdKinMass = MathUtils.standardDeviation(kinMass);

        //Keep only values within 1 standard deviation
        Collections.remove(kinMass, v -> v < meanKinMass + stdKinMass || meanKinMass - stdKinMass < v);

        this.kinMass = MathUtils.averageDouble(kinMass);
    }
}