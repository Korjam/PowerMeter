package com.kinwatt.powermeter.sensor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class GattClientBase {
    private static final String TAG = GattClientBase.class.getSimpleName();

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    private static final UUID SERVER_CHARACTERISTIC_CONFIG = UUID.fromString("00002903-0000-1000-8000-00805F9B34FB");

    private Context context;

    private UUID serviceUuid;
    private List<UUID> notifications = new ArrayList<>();

    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private BluetoothGattService service;

    private List<NotificationListener> listeners = new ArrayList<>();

    public GattClientBase(Context context, BluetoothDevice device, UUID serviceUuid) {
        this.context = context;
        this.device = device;
        this.serviceUuid = serviceUuid;
    }

    public void enableNotifications(UUID uuid) {
        if (service == null) {
            notifications.add(uuid);
        }
        else {
            enableNotificationsInternal(uuid);
        }
    }

    public void connect() {
        gatt = device.connectGatt(context, false, callback);

        if (gatt == null) {
            Log.i(TAG, "Unable to connect GATT server");
        }
        else {
            Log.i(TAG, "Trying to connect GATT server");
        }
    }

    public void close() {
        gatt.close();
        gatt = null;
        service = null;
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }
    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    protected void onNotificationReceived(Characteristic characteristic) {
        for (NotificationListener listener : listeners) {
            listener.onNotificationReceived(characteristic);
        }
    }

    protected abstract Characteristic decode(BluetoothGattCharacteristic characteristic);

    private void enableNotificationsInternal(UUID uuid) {
        enableNotificationsInternal(service.getCharacteristic(uuid));
    }

    private void enableNotificationsInternal(BluetoothGattCharacteristic characteristic) {
        if (gatt.setCharacteristicNotification(characteristic, true)) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + gatt.getDevice().getAddress() + " " + gatt.getDevice().getName());
                gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED");
                if (status == 133) {
                    Log.i(TAG, "Many connections");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "Services discovered");

            service = gatt.getService(serviceUuid);

            for (UUID uuid : notifications) {
                enableNotificationsInternal(uuid);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (notifications.contains(characteristic.getUuid())) {
                onNotificationReceived(decode(characteristic));
            }
        }
    };
}

