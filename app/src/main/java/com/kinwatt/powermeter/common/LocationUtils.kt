package com.kinwatt.powermeter.common

import android.location.Location

import com.kinwatt.powermeter.data.Position
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm
import com.kinwatt.powermeter.common.mathUtils.*

object LocationUtils {

    private val INTERPOLATION_STEP = 1000L // 1s
    private val ALTITUDE_ERROR = 10f

    fun interpolate(p1: Position, p2: Position, v: Long) = Position(
            interpolate(p1.timestamp, p1.latitude,
                        p2.timestamp, p2.latitude, v),
            interpolate(p1.timestamp, p1.longitude,
                        p2.timestamp, p2.longitude, v),
            interpolate(p1.timestamp, p1.altitude,
                        p2.timestamp, p2.altitude, v),
            interpolate(p1.timestamp, p1.speed,
                        p2.timestamp, p2.speed, v), v)

    fun interpolation(p1: Position, p2: Position): (Long) -> Position {
        val latitude = interpolation(
                p1.timestamp, p1.latitude,
                p2.timestamp, p2.latitude)
        val longitude = interpolation(
                p1.timestamp, p1.longitude,
                p2.timestamp, p2.longitude)
        val altitude = interpolation(
                p1.timestamp, p1.altitude,
                p2.timestamp, p2.altitude)
        val speed = interpolation(
                p1.timestamp, p1.speed,
                p2.timestamp, p2.speed)

        return { v -> Position(latitude(v), longitude(v), altitude(v), speed(v), v) }
    }

    fun interpolate(p1: Location, p2: Location, v: Long): Location {

        val res = Location(p1.provider)
        res.latitude = interpolate(
                p1.time, p1.latitude, p2.time, p2.latitude, v)
        res.longitude = interpolate(
                p1.time, p1.longitude,
                p2.time, p2.longitude, v)
        res.altitude = interpolate(
                p1.time, p1.altitude,
                p2.time, p2.altitude, v)
        res.speed = interpolate(
                p1.time, p1.speed,
                p2.time, p2.speed, v)
        res.time = v
        return res
    }

    fun interpolate(p1: Location, p2: Location): (Long) -> Location {
        val latitude = interpolation(
                p1.time, p1.latitude,
                p2.time, p2.latitude)
        val longitude = interpolation(
                p1.time, p1.longitude,
                p2.time, p2.longitude)
        val altitude = interpolation(
                p1.time, p1.altitude,
                p2.time, p2.altitude)
        val speed = interpolation(
                p1.time, p1.speed,
                p2.time, p2.speed)

        val provider = p1.provider

        return { v ->
            val res = Location(provider)
            res.latitude = latitude(v)
            res.longitude = longitude(v)
            res.altitude = altitude(v)
            res.speed = speed(v)
            res.time = v
            res
        }
    }

    @JvmOverloads
    fun interpolate(record: Record, interpolationStep: Long = INTERPOLATION_STEP): Record {
        val res = Record(record.name, record.date)
        res.positions.add(record.positions.first())

        for (i in 0 until record.positions.size - 1) {
            interpolatePositions(res, record.positions[i], record.positions[i + 1], interpolationStep)
        }

        return res
    }

    private fun interpolatePositions(record: Record, p1: Position, p2: Position, interpolationStep: Long) {
        val interpolation = interpolation(p1, p2)

        val start = record.positions.last().timestamp + interpolationStep
        val end = p2.timestamp + (interpolationStep - p2.timestamp % interpolationStep)

        val algorithm = CyclingOutdoorPowerAlgorithm(null);

        for (i in start until end step interpolationStep) {
            val lastPosition = record.positions.last()
            val interpolatedPosition = interpolation(i)

            //TODO: Calculate degrees from 5s ago;
            /*
            if (record.positions.size >= 5) {
                val target = buffer.peek(4)
                val hDiff = interpolatedPosition.altitude - target.altitude
                val grade = hDiff / target.getDistance(interpolatedPosition)

                interpolatedPosition.power = algorithm.calculatePower(lastPosition, interpolatedPosition, grade)
            } else {
                interpolatedPosition.power  = algorithm.calculatePower(lastPosition, interpolatedPosition)
            }
            */

            interpolatedPosition.power = algorithm.calculatePower(Position.convert(lastPosition), Position.convert(interpolatedPosition))

            record.positions.add(interpolatedPosition)
        }
    }

    fun normalize(positions: List<Position>) {
        for (i in positions.indices) {
            normalize(i, positions)
        }
    }

    private fun normalize(i: Int, positions: List<Position>) {
        var position = positions[i]
        val previous = if (i > 0) positions[i - 1] else null

        if (previous != null && position.altitude == 0.0) {
        //if (previous != null && previous.altitude - position.altitude > ALTITUDE_ERROR) {
            val j = getNextValidIndex(i, previous.altitude, positions)

            val next = if (j > 0) positions[j] else null
            if (next != null) {

                val interpolation = interpolation(previous, next)

                for (k in i until j) {
                    position = positions[k]

                    val expected = interpolation(position.timestamp)
                    if (position.speed == 0f) {
                        position.speed = expected.speed
                    }
                    position.altitude = expected.altitude
                }
            }
        }
    }

    private fun getNextValidIndex(i: Int, validAltitude: Double, positions: List<Position>): Int {
        for (j in i until positions.size) {
            if (positions[j].altitude != 0.0) {
            //if (validAltitude - positions[j].altitude() < ALTITUDE_ERROR) {
                return j
            }
        }
        return -1
    }
}
