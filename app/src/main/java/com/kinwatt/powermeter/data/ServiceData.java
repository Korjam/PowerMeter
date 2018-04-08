package com.kinwatt.powermeter.data;

import java.util.UUID;

public class ServiceData {
    private UUID uuid;

    public ServiceData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
