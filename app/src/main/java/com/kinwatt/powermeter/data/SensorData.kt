package com.kinwatt.powermeter.data

import java.util.*

class SensorData(var name: String? = null, var address: String? = null) {

    val services: List<ServiceData> = ArrayList()

    override fun equals(other: Any?) = if (other is SensorData) equals(other) else false

    fun equals(other: SensorData) = this.address == other.address
}

class ServiceData(var uuid: UUID)