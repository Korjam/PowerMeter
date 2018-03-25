package com.kinwatt.powermeter.data;

public class Bike {
    private BikeType type;
    private float weight;

    public Bike() {
    }

    public BikeType getType() {
        return type;
    }
    public void setType(BikeType type) {
        this.type = type;
    }

    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
}
