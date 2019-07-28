package com.kinwatt.powermeter.data.provider

import android.content.Context
import android.hardware.Sensor
import android.widget.Toast

import com.kinwatt.powermeter.data.SensorData
import com.kinwatt.powermeter.data.ServiceData
import com.kinwatt.powermeter.data.mappers.SensorMapper

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.UUID

class SensorProvider private constructor(private val context: Context) {

    private val dataFile: File

    private val sensors = ArrayList<SensorData>()

    val all: List<SensorData>
        get() = sensors

    init {

        dataFile = File(context.filesDir, "devices.json")
        if (dataFile.exists()) {
            try {
                for (data in SensorMapper.load(dataFile)) {
                    if (!sensors.contains(data)) {
                        sensors.add(data)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun findSensor(serviceId: UUID): SensorData? =
        sensors.flatMap { sensor -> sensor.services.map { Pair(sensor, it) } }
               .firstOrNull{ it.second.uuid == serviceId }?.first

    fun add(sensor: SensorData) {
        if (!this.sensors.contains(sensor)) {
            this.sensors.add(sensor)
            try {
                SensorMapper.save(sensors, dataFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun remove(sensor: SensorData): Boolean {
        var res = this.sensors.remove(sensor)
        try {
            SensorMapper.save(sensors, dataFile)
        } catch (e: IOException) {
            e.printStackTrace()
            res = false
        }

        return res
    }

    companion object {

        private var instance: SensorProvider? = null

        fun getProvider(context: Context): SensorProvider {
            if (instance == null) {
                instance = SensorProvider(context)
            }
            return instance!!
        }
    }
}
