package com.kinwatt.powermeter.data;

import java.util.ArrayList;
import java.util.List;

public class SensorData {
    private String name;
    private String address;
    private List<ServiceData> services;

    public SensorData() {
        services = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public List<ServiceData> getServices() {
        return services;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  SensorData) {
            SensorData other = (SensorData)obj;
            return this.address.equals(other.address);
        }
        else  {
            return false;
        }
    }
}
