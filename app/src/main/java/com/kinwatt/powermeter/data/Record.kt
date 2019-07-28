package com.kinwatt.powermeter.data

import android.location.Location
import com.kinwatt.powermeter.data.mappers.DateSerializer
import kotlinx.serialization.Serializable

import java.util.ArrayList
import java.util.Date

@Serializable
data class Record constructor(var name: String = "",
                              @Serializable(with=DateSerializer::class) var date: Date = Date()) {

    val positions: ArrayList<Position> = ArrayList()

    val distance: Float
        get() {
            var distance = 0f
            for (i in 0 until positions.size - 1) {
                val p1 = positions[i]
                val p2 = positions[i + 1]
                distance += p1.distanceTo(p2)
            }
            return distance
        }

    val speed: Float
        get() = positions.asSequence()
                .filter { it.speed != 0f }
                .map { it.speed }
                .average().toFloat()

    fun addPosition(location: Location): Position {
        if (this.positions.isEmpty()) {
            this.date = Date(location.time)
        }
        val position = Position(location.latitude, location.longitude, location.altitude,
                location.speed, location.time - date.time)
        positions.add(position)
        return position
    }
}
