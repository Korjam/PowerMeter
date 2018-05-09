package com.kinwatt.powermeter.data;

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

    public void calibrateTrainer(List<Float> speed, List<Integer> power){

        //Copy speed (in m/s) and power in new lists (we are going to remove some data from them)
        List<Float> speedCopy = new ArrayList<>();
        List<Integer> powerCopy = new ArrayList<>();
        for (int i = 0; i < power.size(); i++ ){
            speedCopy.add(speed.get(i) / 3.6f);
            powerCopy.add(power.get(i));
        }

        //remove pairs (speed,power) in which power=0
        for (int i = power.size() - 1; i >= 0; i--) {
            if (power.get(i) == 0) {
                power.remove(i);
                speed.remove(i);
            }
        }

        //Speed from kmh to m/s and define speed3 as speed^3
        List<Float> speed3 = new ArrayList<>();
        for (int i=0; i < speed.size(); i++){
            speed.set(i, speed.get(i) / 3.6f);
            speed3.add((float) Math.pow(speed.get(i), 3));
        }

        double[][] matrixX = new double[2][speed.size()];
        double[][] XtX = new double[2][2];
        double[][] invXtX = new double[2][2];
        double[][] pseudoinverse = new double[2][speed.size()];

        //Matrix X
        for (int i = 0; i < speed.size(); i++) {
            matrixX[0][i] = speed.get(i);
            matrixX[1][i] = speed3.get(i);
        }

        //Matrix X'X
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
        invXtX[0][0] = XtX[1][1] / det;
        invXtX[0][1] = -XtX[0][1] / det;
        invXtX[1][0] = invXtX[0][1];
        invXtX[1][1] = XtX[0][0] / det;

        //Calculating matrix inv(XtX)Xt (Moore–Penrose pseudoinverse)
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < speed.size(); j++){
                pseudoinverse[i][j] = invXtX[i][0] * speed.get(j) + invXtX[i][1] * speed3.get(j);
            }
        }

        //Obtaining Crr and CdA finally.
        double Crr = 0, CdA = 0;
        for (int i = 0; i < speed.size(); i++){
            Crr += pseudoinverse[0][i] * power.get(i);
            CdA += pseudoinverse[1][i] * power.get(i);
        }

        //Calculate estimates of kinMass for suitable candidates
        List<Double> kinMass = new ArrayList<Double>();
        for (int i=1; i < powerCopy.size(); i++){
            if (powerCopy.get(i) == 0 && powerCopy.get(i-1) == 0 && speedCopy.get(i) > 0 && speedCopy.get(i).intValue() != speedCopy.get(i-1).intValue()){
                double mass = (Crr * speedCopy.get(i)+CdA * Math.pow(speedCopy.get(i),3))/(Math.pow(speedCopy.get(i-1),2) - Math.pow(speedCopy.get(i),2));
                kinMass.add(mass);
            }
        }

        //Remove kinMass elements i such that kinMass(i)<0
        for (int i = kinMass.size() - 1; i >= 0; i--) {
            if (kinMass.get(i) < 0) {
                kinMass.remove(i);
            }
        }

        //Calculate mean of kinMass
        double meanKinMass = 0;
        for (int i = 0; i < kinMass.size(); i++){
            meanKinMass += kinMass.get(i);
        }
        meanKinMass = meanKinMass / kinMass.size();

        //Calculate standard deviation
        double stdKinMass;
        double sum = 0;
        for (int i = 0; i < kinMass.size(); i++){
            sum += Math.pow(kinMass.get(i)-meanKinMass,2);
        }
        stdKinMass = Math.pow(sum/(kinMass.size()-1),0.5);

        //Keep only values within 1 standard deviation
        for (int i = kinMass.size() - 1; i >= 0; i--) {
            if (kinMass.get(i) < meanKinMass + stdKinMass || meanKinMass -stdKinMass < kinMass.get(i)) {
                kinMass.remove(i);
            }
        }

        //Calculate mean again
        meanKinMass = 0;
        for (int i = 0; i < kinMass.size(); i++){
            meanKinMass += kinMass.get(i);
        }

        this.cRolling = Crr;
        this.CdA = CdA;
        this.kinMass = meanKinMass;
    }
}