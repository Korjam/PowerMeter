package com.kinwatt.powermeter.ui

interface ActivityView {
    fun setSpeed(speed: Float)
    fun setAltitude(altitude: Double)
    fun setDistance(distance: Float)
    fun setPowerAverage3(power: Float)
    fun setPowerAverage5(power: Float)
    fun setPowerAverage10(power: Float)
}
