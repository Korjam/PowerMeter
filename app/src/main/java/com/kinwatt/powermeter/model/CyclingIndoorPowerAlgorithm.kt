package com.kinwatt.powermeter.model

import android.location.Location

import com.kinwatt.powermeter.data.User

import kotlin.math.pow

class CyclingIndoorPowerAlgorithm(user: User?) : PowerAlgorithm {

    override fun calculatePower(pos1: Location, pos2: Location): Float = calculatePower(pos1, pos2, 0.0)

    override fun calculatePower(pos1: Location, pos2: Location, grade: Double): Float {
        val avgSpeed = floatArrayOf(pos1.speed, pos2.speed).average()
        val Pkin = kinMass * (pos2.speed.pow(2) - pos1.speed.pow(2))
        val Pr = (cRolling * avgSpeed)
        val Pd = CdA * avgSpeed.pow(3)
        return maxOf(0.0, Pkin + Pr + Pd).toInt().toFloat()
    }

    companion object {

        private const val cRolling = 5.97
        private const val CdA = 0.179
        private const val kinMass = 3.5
    }
}
