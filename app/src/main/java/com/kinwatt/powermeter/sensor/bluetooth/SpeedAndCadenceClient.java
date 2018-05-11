package com.kinwatt.powermeter.sensor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.kinwatt.powermeter.sensor.SpeedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpeedAndCadenceClient extends GattClientBase {
    public static final UUID SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB");

    private List<SpeedListener> listeners = new ArrayList<>();

    private SpeedAndCadenceMeasure lastMeasure;
    private float lastRpm;

    public SpeedAndCadenceClient(Context context, BluetoothDevice device) {
        super(context, device, SERVICE_UUID);
        enableNotifications(SpeedAndCadenceMeasure.CHARACTERISTIC_UUID);
    }

    public void addListener(SpeedListener listener) {
        listeners.add(listener);
    }
    public void removeListener(SpeedListener listener) {
        listeners.remove(listener);
    }

    protected void onSpeedChanged(float speed) {
        for (SpeedListener listener : listeners) {
            listener.onSpeedChanged(speed);
        }
    }

    @Override
    protected void onNotificationReceived(Characteristic characteristic) {
        super.onNotificationReceived(characteristic);

        SpeedAndCadenceMeasure currentMeasure = (SpeedAndCadenceMeasure)characteristic;
        if (lastMeasure != null) {
            float rpm = currentMeasure.getRpm(lastMeasure);
            if (Float.isNaN(rpm)) {
                rpm = 0;
            }
            if (rpm != lastRpm) {
                onSpeedChanged(rpm);
            }
            lastRpm = rpm;
        }

        lastMeasure = currentMeasure;
    }

    @Override
    protected Characteristic decode(BluetoothGattCharacteristic characteristic) {
        return SpeedAndCadenceMeasure.decode(characteristic);
    }
}
