package com.kinwatt.powermeter.sensor.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class SpeedAndCadenceMeasure extends Characteristic {
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805F9B34FB");

    private static final int MAX_SECONDS = 64; //0xFFFF / 1024

    private int wheelRevolutions;
    private float wheelRevolutionsEventTime;

    private int crankRevolutions;
    private float crankRevolutionsEventTime;

    private SpeedAndCadenceMeasure() {
        super(CHARACTERISTIC_UUID);
    }

    public int getWheelRevolutions() {
        return wheelRevolutions;
    }
    public void setWheelRevolutions(int wheelRevolutions) {
        this.wheelRevolutions = wheelRevolutions;
    }

    public float getWheelRevolutionsEventTime() {
        return wheelRevolutionsEventTime;
    }
    public void setWheelRevolutionsEventTime(float wheelRevolutionsEventTime) {
        this.wheelRevolutionsEventTime = wheelRevolutionsEventTime;
    }

    public int getCrankRevolutions() {
        return crankRevolutions;
    }
    public void setCrankRevolutions(int crankRevolutions) {
        this.crankRevolutions = crankRevolutions;
    }

    public float getCrankRevolutionsEventTime() {
        return crankRevolutionsEventTime;
    }
    public void setCrankRevolutionsEventTime(float crankRevolutionsEventTime) {
        this.crankRevolutionsEventTime = crankRevolutionsEventTime;
    }

    public float getRpm(SpeedAndCadenceMeasure csc) {
        float dt = this.wheelRevolutionsEventTime - csc.wheelRevolutionsEventTime;
        if (csc.wheelRevolutionsEventTime  > this.wheelRevolutionsEventTime) {
            dt += MAX_SECONDS;
        }
        float dr = this.wheelRevolutions - csc.wheelRevolutions;

        return dr / dt * 60;
    }

    public float getCadence(SpeedAndCadenceMeasure csc) {
        float dt = this.crankRevolutionsEventTime - csc.crankRevolutionsEventTime;
        if (csc.crankRevolutionsEventTime  > this.crankRevolutionsEventTime) {
            dt += MAX_SECONDS;
        }
        float dr = this.crankRevolutions - csc.crankRevolutions;

        return dr / dt * 60;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpeedAndCadenceMeasure) {
            SpeedAndCadenceMeasure other = (SpeedAndCadenceMeasure)obj;
            return this.crankRevolutionsEventTime == other.crankRevolutionsEventTime &&
                    this.crankRevolutions == other.crankRevolutions &&
                    this.wheelRevolutionsEventTime == other.wheelRevolutionsEventTime &&
                    this.wheelRevolutions == other.wheelRevolutions;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (wheelRevolutions != 0) {
            sb.append(String.format("[%s] wheelRevolutions: %s", wheelRevolutionsEventTime, wheelRevolutions));
        }
        if (crankRevolutions != 0) {
            if (sb.length() != 0) {
                sb.append(" , ");
            }
            sb.append(String.format("[%s] crankRevolutions: %s", crankRevolutionsEventTime, crankRevolutions));
        }
        return sb.toString();
    }

    /**
     * Decoded as defined in bluetooth docs.
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.csc_measurement.xml">Bluetooth docs</a>
     * @param characteristic
     * @return
     */
    public static SpeedAndCadenceMeasure decode(BluetoothGattCharacteristic characteristic) {
        SpeedAndCadenceMeasure res = new SpeedAndCadenceMeasure();

        int offset = 0;

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);

        boolean c1 = (flags & 1) == 1;
        boolean c2 = (flags & 2) == 2;

        offset++;

        if (c1) {
            res.wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            res.wheelRevolutionsEventTime = (float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 1024;
            offset += 2;
        }

        if (c2) {
            res.crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            res.crankRevolutionsEventTime = (float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 1024;
        }

        return res;
    }
}
