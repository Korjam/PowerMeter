package com.kinwatt.powermeter.sensor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.kinwatt.powermeter.model.PowerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PowerClient extends GattClientBase {
    public static final UUID SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB");

    private List<PowerListener> listeners = new ArrayList<>();

    public PowerClient(Context context, BluetoothDevice device) {
        super(context, device, SERVICE_UUID);
        enableNotifications(PowerMeasure.CHARACTERISTIC_UUID);
    }

    public void addListener(PowerListener listener) {
        listeners.add(listener);
    }
    public void removeListener(PowerListener listener) {
        listeners.remove(listener);
    }

    protected void onPowerChanged(long time, float power) {
        for (PowerListener listener : listeners) {
            listener.onPowerMeasured(time, power);
        }
    }

    @Override
    protected void onNotificationReceived(Characteristic characteristic) {
        super.onNotificationReceived(characteristic);

        PowerMeasure currentMeasure = (PowerMeasure)characteristic;
        onPowerChanged((long) currentMeasure.getCrankRevolutionsEventTime(), currentMeasure.getInstantaneousPower());
    }

    @Override
    protected Characteristic decode(BluetoothGattCharacteristic characteristic) {
        return PowerMeasure.decode(characteristic);
    }
}
