package com.kinwatt.powermeter.sensor.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

import java.util.ArrayList
import java.util.UUID

abstract class GattClientBase(private val context: Context, private val device: BluetoothDevice, private val serviceUuid: UUID) {
    private val notifications = ArrayList<UUID>()
    private var gatt: BluetoothGatt? = null
    private var service: BluetoothGattService? = null

    private val listeners = ArrayList<NotificationListener>()

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + gatt.device.address + " " + gatt.device.name)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED")
                if (status == 133) {
                    Log.i(TAG, "Many connections")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i(TAG, "Services discovered")

            service = gatt.getService(serviceUuid)

            for (uuid in notifications) {
                enableNotificationsInternal(uuid)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (notifications.contains(characteristic.uuid)) {
                onNotificationReceived(decode(characteristic))
            }
        }
    }

    fun enableNotifications(uuid: UUID) {
        if (service == null) {
            notifications.add(uuid)
        } else {
            enableNotificationsInternal(uuid)
        }
    }

    fun connect() {
        gatt = device.connectGatt(context, false, callback)

        if (gatt == null) {
            Log.i(TAG, "Unable to connect GATT server")
        } else {
            Log.i(TAG, "Trying to connect GATT server")
        }
    }

    fun close() {
        gatt!!.close()
        gatt = null
        service = null
    }

    fun addListener(listener: NotificationListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NotificationListener) {
        listeners.remove(listener)
    }

    protected open fun onNotificationReceived(characteristic: Characteristic) {
        for (listener in listeners) {
            listener.onNotificationReceived(characteristic)
        }
    }

    protected abstract fun decode(characteristic: BluetoothGattCharacteristic): Characteristic

    private fun enableNotificationsInternal(uuid: UUID) {
        enableNotificationsInternal(service!!.getCharacteristic(uuid))
    }

    private fun enableNotificationsInternal(characteristic: BluetoothGattCharacteristic) {
        if (gatt!!.setCharacteristicNotification(characteristic, true)) {
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt!!.writeDescriptor(descriptor)
        }
    }

    companion object {
        private val TAG = GattClientBase::class.java.simpleName

        private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        private val SERVER_CHARACTERISTIC_CONFIG = UUID.fromString("00002903-0000-1000-8000-00805F9B34FB")
    }
}

