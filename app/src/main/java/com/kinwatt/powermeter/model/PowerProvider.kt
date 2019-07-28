package com.kinwatt.powermeter.model

import android.location.Location

import com.kinwatt.powermeter.common.LocationUtils
import com.kinwatt.powermeter.sensor.LocationListener
import com.kinwatt.powermeter.sensor.LocationProvider
import java.util.*

class PowerProvider(var powerAlgorithm: PowerAlgorithm, private val locationProvider: LocationProvider) : LocationListener {

    private var baseTime: Long = 0
    private var previousLocation: Location? = null
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
        previousLocation = null
        baseTime = 0
        buffer.clear()
    }

    override fun onLocationChanged(location: Location) {
        if (previousLocation == null) baseTime = location.time

        val location = Location(location)
        location.time = location.time - baseTime

        if (previousLocation != null)
            interpolatePositions(location)
        else
            buffer.add(location)

        previousLocation = location
    }

    protected fun onPowerCalculated(time: Long, power: Float) {
        for (listener in listeners) {
            listener.onPowerMeasured(time, power)
        }
    }

    private fun interpolatePositions(position: Location) {
        val interpolation = LocationUtils.interpolate(previousLocation!!, position)

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
        protected val Logger = java.util.logging.Logger.getLogger("PowerProvider")!!

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
