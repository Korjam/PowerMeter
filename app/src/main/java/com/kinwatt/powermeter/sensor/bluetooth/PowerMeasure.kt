package com.kinwatt.powermeter.sensor.bluetooth

import android.bluetooth.BluetoothGattCharacteristic

import java.util.UUID

class PowerMeasure private constructor() : Characteristic(CHARACTERISTIC_UUID) {

    var instantaneousPower: Int = 0
    private var pedalPowerBalance: Float = 0.toFloat()
    private var accumulatedTorque: Float = 0.toFloat()

    private var pedalPowerReferencePresent: PowerReference? = null

    private var wheelRevolutions: Int = 0
    private var wheelRevolutionsEventTime: Float = 0.toFloat()

    private var crankRevolutions: Int = 0
    var crankRevolutionsEventTime: Float = 0.toFloat()

    internal var minimumForce: Int = 0
    internal var maximumForce: Int = 0
    internal var minimumTorque: Int = 0
    internal var maximumTorque: Int = 0
    internal var minimumAngle: Int = 0
    internal var maximumAngle: Int = 0

    internal var topDeadSpotAngle: Int = 0
    internal var bottomDeadSpotAngle: Int = 0

    internal var accumulatedEnergy: Int = 0

    override fun toString() = "crankRevolutions = $crankRevolutions, " +
            "pedalPowerBalance = $pedalPowerBalance, " +
            "instantaneousPower = $instantaneousPower, " +
            "pedalPowerReference = $pedalPowerReferencePresent"

    enum class PowerReference {
        Left,
        Unkown
    }

    companion object {
        val CHARACTERISTIC_UUID = UUID.fromString("00002A63-0000-1000-8000-00805F9B34FB")

        /**
         * Decoded as defined in bluetooth docs.
         * @see [Bluetooth docs](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.cycling_power_measurement.xml)
         *
         * @param characteristic
         * @return
         */
        fun decode(characteristic: BluetoothGattCharacteristic): PowerMeasure {
            val res = PowerMeasure()

            var offset = 0

            val flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)!!

            val pedalPowerBalancePresent = flags and 1 == 1
            res.pedalPowerReferencePresent = if (flags and 2 == 2) PowerReference.Left else PowerReference.Unkown
            val accumulatedTorquePresent = flags and 4 == 4
            val accumulatedTorqueSource = flags and 8 == 8 // true = crank based , false = wheel based
            val wheelRevolutionDataPresent = flags and 16 == 16
            val crankRevolutionDataPresent = flags and 32 == 32
            val extremeForceMagnitudesPresent = flags and 64 == 64
            val extremeTorqueMagnitudesPresent = flags and 128 == 128
            val extremeAnglesPresent = flags and 256 == 256
            val topDeadSpotAnglePresent = flags and 512 == 512
            val bottomDeadSpotAnglePresent = flags and 1024 == 1024
            val accumulatedEnergyPresent = flags and 2048 == 2048
            val offsetCompensationIndicator = flags and 4096 == 4096
            offset += 2

            res.instantaneousPower = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)!!
            offset += 2

            if (pedalPowerBalancePresent) {
                res.pedalPowerBalance = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset) as Float / 2
                offset++
            }

            if (accumulatedTorquePresent) {
                res.accumulatedTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) as Float / 32
                offset += 2
            }

            if (wheelRevolutionDataPresent) {
                res.wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset)!!
                offset += 4
                res.wheelRevolutionsEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) as Float / 1024
                offset += 2
            }

            if (crankRevolutionDataPresent) {
                res.crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)!!
                offset += 2
                res.crankRevolutionsEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) as Float / 1024
                offset += 2
            }

            if (extremeForceMagnitudesPresent) {
                res.maximumForce = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)!!
                offset += 2
                res.minimumForce = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)!!
                offset += 2
            }

            if (extremeTorqueMagnitudesPresent) {
                res.maximumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)!! / 32
                offset += 2
                res.minimumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)!! / 32
                offset += 2
            }

            if (extremeAnglesPresent) {
                offset += 3

                //TODO : decode UINT_12 values
                /*
                res.maximumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT12, offset);
                offset += 1.5;
                res.minimumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT12, offset);
                offset += 1.5;*/
            }

            if (topDeadSpotAnglePresent) {
                res.topDeadSpotAngle = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)!!
                offset += 2
            }

            if (bottomDeadSpotAnglePresent) {
                res.bottomDeadSpotAngle = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)!!
                offset += 2
            }

            if (accumulatedEnergyPresent) {
                res.accumulatedEnergy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)!! * 1000
            }

            return res
        }
    }
}
