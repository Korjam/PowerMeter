package com.kinwatt.powermeter.model

import android.content.Context
import android.location.Location

import com.kinwatt.powermeter.common.LocationUtils
import com.kinwatt.powermeter.common.mathUtils.*
import com.kinwatt.powermeter.sensor.LocationListener
import com.kinwatt.powermeter.sensor.LocationProvider
import java.util.*

class PowerProvider(var powerAlgorithm: PowerAlgorithm, private val locationProvider: LocationProvider) : LocationListener {

    private var baseTime: Long = 0
    private var location1: Location? = null
    private var location2: Location? = null
    private val buffer = ArrayDeque<Location>(10)

    protected var listeners: MutableList<PowerListener> = ArrayList()

    init {
        this.locationProvider.addListener(this)

        reset()
    }

    fun addListener(listener: PowerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PowerListener) {
        listeners.remove(listener)
    }

    fun reset() {
        location1 = null
        location2 = null
        baseTime = 0
        buffer.clear()
    }

    override fun onLocationChanged(location: Location) {
        if (location2 == null) baseTime = location.time

        val location = Location(location)
        location.time = location.time - baseTime

        if (location.altitude == 0.0 && location1 != null && location2 != null) {
            softenAltitude(location)
        }

        if (location2 != null)
            interpolatePositions(location)
        else
            buffer.add(location)

        location1 = location2
        location2 = location
    }

    protected fun onPowerCalculated(time: Long, power: Float) {
        for (listener in listeners) {
            listener.onPowerMeasured(time, power)
        }
    }

    private fun softenAltitude(position: Location) {
        val altitude = interpolation(
                location1!!.time, location1!!.altitude,
                location2!!.time, location2!!.altitude)

        position.altitude = altitude(position.time)

        if (position.speed == 0f) {
            val speed = interpolation(
                    location1!!.time, location1!!.speed,
                    location2!!.time, location2!!.speed)
            position.speed = speed(position.time)
        }
    }

    private fun interpolatePositions(position: Location) {
        val interpolation = LocationUtils.interpolate(location2!!, position)

        val start = buffer.peekLast().time + INTERPOLATION_STEP
        val end = position.time + (INTERPOLATION_STEP - position.time % INTERPOLATION_STEP)

        for (i in start until end step INTERPOLATION_STEP) {
            val interpolatedPosition = interpolation(i)
            val lastPosition = buffer.peekLast()

            val power = getPower(lastPosition, interpolatedPosition)

            buffer.add(interpolatedPosition)

            onPowerCalculated(interpolatedPosition.time + baseTime, power)
        }
    }

    private fun getPower(l1: Location, l2: Location): Float {
        //TODO: Calculate degrees from 5s ago;
        /*
        if (buffer.size >= 5) {
            val target = buffer.elementAt(4)

            val hDiff = l2.altitude - target.altitude
            val grade = hDiff / target.distanceTo(l2)

            return powerAlgorithm.calculatePower(l1, l2, grade)
        } else {
            return powerAlgorithm.calculatePower(l1, l2)
        }
        */
        return powerAlgorithm.calculatePower(l1, l2)
    }

    companion object {

        private const val INTERPOLATION_STEP = 1000L // 1s

        private fun getEndTime(base: Long, target: Long): Long {
            var res = base
            while (res < target) {
                res += INTERPOLATION_STEP
            }
            return res
        }
    }
}
