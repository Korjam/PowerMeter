package com.kinwatt.powermeter.sensor.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context

import com.kinwatt.powermeter.model.PowerListener

import java.util.ArrayList
import java.util.UUID

class PowerClient(context: Context, device: BluetoothDevice) : GattClientBase(context, device, SERVICE_UUID) {

    private val listeners = ArrayList<PowerListener>()

    init {
        enableNotifications(PowerMeasure.CHARACTERISTIC_UUID)
    }

    fun addListener(listener: PowerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PowerListener) {
        listeners.remove(listener)
    }

    protected fun onPowerChanged(time: Long, power: Float) {
        for (listener in listeners) {
            listener.onPowerMeasured(time, power)
        }
    }

    override fun onNotificationReceived(characteristic: Characteristic) {
        super.onNotificationReceived(characteristic)

        val currentMeasure = characteristic as PowerMeasure
        onPowerChanged(currentMeasure.crankRevolutionsEventTime.toLong(), currentMeasure.instantaneousPower.toFloat())
    }

    override fun decode(characteristic: BluetoothGattCharacteristic): Characteristic {
        return PowerMeasure.decode(characteristic)
    }

    companion object {
        val SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB")
    }
}
