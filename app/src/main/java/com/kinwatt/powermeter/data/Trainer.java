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

        //remove pairs (speed,power) in which power=0
        for (int i = power.size() - 1; i >= 0; i--) {
            if (power.get(i) == 0) {
                power.remove(i);
                speed.remove(i);
            }
        }

        //Speed from kmh to ms and define speed3 as speed^3
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

        double Crr = 0, CdA = 0;
        for (int i = 0; i < speed.size(); i++){
            Crr += pseudoinverse[0][i] * power.get(i);
            CdA += pseudoinverse[1][i] * power.get(i);
        }

        this.cRolling = Crr;
        this.CdA = CdA;
    }




}