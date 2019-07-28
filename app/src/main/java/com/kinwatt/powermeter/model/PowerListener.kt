package com.kinwatt.powermeter.model

interface PowerListener {
    fun onPowerMeasured(time: Long, power: Float)
}
