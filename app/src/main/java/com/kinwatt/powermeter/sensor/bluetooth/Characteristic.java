package com.kinwatt.powermeter.sensor.bluetooth;

import java.util.UUID;

public abstract class Characteristic {
    private UUID mUuid;

    public Characteristic(UUID uuid) {
        mUuid = uuid;
    }

    public UUID getUuid() {
        return mUuid;
    }
    //protected abstract int decode(int offset);
}
