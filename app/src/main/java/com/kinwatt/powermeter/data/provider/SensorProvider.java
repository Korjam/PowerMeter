package com.kinwatt.powermeter.data.provider;

import android.content.Context;
import android.hardware.Sensor;
import android.widget.Toast;

import com.kinwatt.powermeter.data.SensorData;
import com.kinwatt.powermeter.data.ServiceData;
import com.kinwatt.powermeter.data.mappers.SensorMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorProvider {

    private static SensorProvider instance;

    private Context context;

    private File dataFile;

    private List<SensorData> sensors = new ArrayList<>();

    private SensorProvider(Context context) {
        this.context = context;

        dataFile = new File(context.getFilesDir(), "devices.json");
        if (dataFile.exists()) {
            try {
                for (SensorData data : SensorMapper.load(dataFile)) {
                    if (!sensors.contains(data)) {
                        sensors.add(data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<SensorData> getAll() {
        return sensors;
    }

    public SensorData findSensor(UUID serviceId) {
        for (SensorData sensor : sensors) {
            for (ServiceData service : sensor.getServices()) {
                if (service.getUuid().equals(serviceId)) {
                    return sensor;
                }
            }
        }
        return null;
    }

    public void add(SensorData sensor) {
        if (!this.sensors.contains(sensor)) {
            this.sensors.add(sensor);
            try {
                SensorMapper.save(sensors, dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean remove(SensorData sensor) {
        boolean res = this.sensors.remove(sensor);
        try {
            SensorMapper.save(sensors, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    public static SensorProvider getProvider(Context context) {
        if (instance == null) {
            instance = new SensorProvider(context);
        }
        return instance;
    }
}
