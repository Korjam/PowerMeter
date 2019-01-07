package com.kinwatt.powermeter.sensor.bluetooth

interface NotificationListener {
    fun onNotificationReceived(characteristic: Characteristic)
}
