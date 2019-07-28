package com.kinwatt.powermeter.sensor.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context

import com.kinwatt.powermeter.sensor.SpeedListener

import java.util.ArrayList
import java.util.UUID

class SpeedAndCadenceClient(context: Context, device: BluetoothDevice) : GattClientBase(context, device, SERVICE_UUID) {

    private val listeners = ArrayList<SpeedListener>()

    private var lastMeasure: SpeedAndCadenceMeasure? = null
    private var lastRpm: Float = 0.toFloat()

    init {
        enableNotifications(SpeedAndCadenceMeasure.CHARACTERISTIC_UUID)
    }

    fun addListener(listener: SpeedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SpeedListener) {
        listeners.remove(listener)
    }

    protected fun onSpeedChanged(speed: Float) {
        for (listener in listeners) {
            listener.onSpeedChanged(speed)
        }
    }

    override fun onNotificationReceived(characteristic: Characteristic) {
        super.onNotificationReceived(characteristic)

        val currentMeasure = characteristic as SpeedAndCadenceMeasure
        if (lastMeasure != null) {
            var rpm = currentMeasure.getRpm(lastMeasure!!)
            if (java.lang.Float.isNaN(rpm)) {
                rpm = 0f
            }
            if (rpm != lastRpm) {
                onSpeedChanged(rpm)
            }
            lastRpm = rpm
        }

        lastMeasure = currentMeasure
    }

    override fun decode(characteristic: BluetoothGattCharacteristic): Characteristic {
        return SpeedAndCadenceMeasure.decode(characteristic)
    }

    companion object {
        val SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB")
    }
}
