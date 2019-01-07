package com.kinwatt.powermeter.sensor.bluetooth

import android.bluetooth.BluetoothGattCharacteristic

import java.util.UUID

class SpeedAndCadenceMeasure private constructor() : Characteristic(CHARACTERISTIC_UUID) {

    var wheelRevolutions: Int = 0
    var wheelRevolutionsEventTime: Float = 0F

    var crankRevolutions: Int = 0
    var crankRevolutionsEventTime: Float = 0F

    fun getRpm(csc: SpeedAndCadenceMeasure): Float {
        var dt = this.wheelRevolutionsEventTime - csc.wheelRevolutionsEventTime
        if (csc.wheelRevolutionsEventTime > this.wheelRevolutionsEventTime) {
            dt += MAX_SECONDS.toFloat()
        }
        val dr = (this.wheelRevolutions - csc.wheelRevolutions).toFloat()

        return dr / dt * 60
    }

    fun getCadence(csc: SpeedAndCadenceMeasure): Float {
        var dt = this.crankRevolutionsEventTime - csc.crankRevolutionsEventTime
        if (csc.crankRevolutionsEventTime > this.crankRevolutionsEventTime) {
            dt += MAX_SECONDS.toFloat()
        }
        val dr = (this.crankRevolutions - csc.crankRevolutions).toFloat()

        return dr / dt * 60
    }

    override fun equals(other: Any?) = when (other) {
        is SpeedAndCadenceMeasure ->
            this.crankRevolutionsEventTime == other.crankRevolutionsEventTime &&
            this.crankRevolutions == other.crankRevolutions &&
            this.wheelRevolutionsEventTime == other.wheelRevolutionsEventTime &&
            this.wheelRevolutions == other.wheelRevolutions
        else -> false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (wheelRevolutions != 0) {
            sb.append(String.format("[%s] wheelRevolutions: %s", wheelRevolutionsEventTime, wheelRevolutions))
        }
        if (crankRevolutions != 0) {
            if (sb.isNotEmpty()) {
                sb.append(" , ")
            }
            sb.append(String.format("[%s] crankRevolutions: %s", crankRevolutionsEventTime, crankRevolutions))
        }
        return sb.toString()
    }

    companion object {
        val CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805F9B34FB")

        private val MAX_SECONDS = 64 //0xFFFF / 1024

        /**
         * Decoded as defined in bluetooth docs.
         * @see [Bluetooth docs](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.csc_measurement.xml)
         *
         * @param characteristic
         * @return
         */
        fun decode(characteristic: BluetoothGattCharacteristic): SpeedAndCadenceMeasure {
            val res = SpeedAndCadenceMeasure()

            var offset = 0

            val flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)!!

            val c1 = flags and 1 == 1
            val c2 = flags and 2 == 2

            offset++

            if (c1) {
                res.wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset)!!
                offset += 4
                res.wheelRevolutionsEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) as Float / 1024
                offset += 2
            }

            if (c2) {
                res.crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset)!!
                offset += 4
                res.crankRevolutionsEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) as Float / 1024
                offset += 4
            }

            return res
        }
    }
}
