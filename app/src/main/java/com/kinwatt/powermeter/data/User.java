package com.kinwatt.powermeter.data;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String name;
    private int age;
    private float weight;
    private int height;
    private List<Bike> bikes;

    public User() {
        bikes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public List<Bike> getBikes() {
        return bikes;
    }
}