package com.kinwatt.powermeter.data

import com.kinwatt.powermeter.data.mappers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SensorData(var name: String? = null, var address: String? = null) {

    val services: MutableList<ServiceData> = ArrayList()

    override fun equals(other: Any?) = if (other is SensorData) equals(other) else false

    fun equals(other: SensorData) = this.address == other.address
}

@Serializable
data class ServiceData(@Serializable(with= UUIDSerializer::class)var uuid: UUID)