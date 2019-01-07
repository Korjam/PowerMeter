package com.kinwatt.powermeter.data

import android.location.Location

import kotlin.math.*

class Position @JvmOverloads constructor(var latitude: Float, var longitude: Float, var altitude: Float = 0f,
                                         var speed: Float = 0f, var timestamp: Long = 0, var power: Float = 0f) : Cloneable {

    fun distanceTo(position: Position): Float {
        val meanLat = floatArrayOf(this.latitude, position.latitude).average()
        val dLat = (position.latitude - this.latitude) * geodesica_u
        val dLon = (position.longitude - this.longitude) * sin(Math.toRadians(90 - meanLat)) * geodesica_u
        return sqrt(dLat.pow(2) + dLon.pow(2)).toFloat()
    }

    override fun equals(other: Any?) = if (other is Position) this.equals(other) else false

    fun equals(other: Position): Boolean {
        return this.altitude == other.altitude &&
                this.latitude == other.latitude &&
                this.longitude == other.longitude &&
                this.speed == other.speed &&
                this.timestamp == other.timestamp &&
                this.power == other.power
    }

    override fun clone() = Position(this.latitude, this.longitude, this.altitude, this.speed, this.timestamp, this.power)

    companion object {

        private const val geodesica = 40030000
        private const val geodesica_u = geodesica / 360

        fun convert(location: Location) = Position(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitude.toFloat(),
                location.speed)

        fun convert(position: Position): Location {
            val res = Location("NULL")
            res.latitude = position.latitude.toDouble()
            res.longitude = position.longitude.toDouble()
            res.altitude = position.altitude.toDouble()
            res.speed = position.speed
            return res
        }
    }
}
