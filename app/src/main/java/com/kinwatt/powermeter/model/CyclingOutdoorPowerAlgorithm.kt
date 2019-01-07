package com.kinwatt.powermeter.model

import android.location.Location

import com.kinwatt.powermeter.data.BikeType
import com.kinwatt.powermeter.data.User

import kotlin.math.*

class CyclingOutdoorPowerAlgorithm(user: User?) : PowerAlgorithm {
    private var cRolling: Double = 0.0
    private var CdA: Double = 0.0
    private var totalMass: Float = 0.0f
    private val drag = CdA * rho / 2

    init {
        if (user != null) {
            val currentBike = user.bikes[0]
            totalMass = user.weight + currentBike.weight

            var percent = 1f
            when (currentBike.type) {
                BikeType.Road -> {
                    percent = 0.8f
                    cRolling = 0.004
                }
                BikeType.Mountain -> {
                    percent = 1f
                    cRolling = 0.008
                }
            }
            CdA = (percent * (0.0285 * (user.height.toDouble() / 100).pow(0.725) *
                    user.weight.toDouble().pow(0.425) + 0.17))
        } else {
            totalMass = 80f
            CdA = 0.36
            cRolling = 0.005
        }
    }

    override fun calculatePower(pos1: Location, pos2: Location): Float {
        // Calculating increments
        // It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.
        val grade = (pos2.altitude - pos1.altitude) / pos1.distanceTo(pos2)
        return calculatePower(pos1, pos2, grade)
    }

    override fun calculatePower(pos1: Location, pos2: Location, grade: Double): Float {
        val beta = atan(grade)

        // Calculate power from p1.getSeconds()... p2.getSeconds()-1;
        val avgSpeed = floatArrayOf(pos2.speed, pos1.speed).average()
        val pKin = (pos2.speed.pow(2) - pos1.speed.pow(2)) * totalMass / 2
        val pGravity = avgSpeed * gForce * totalMass * sin(beta)
        val pDrag = avgSpeed.pow(3) * drag
        val pRolling = avgSpeed * cRolling * gForce * totalMass * cos(beta)
        var power = maxOf(pKin + pGravity + pDrag + pRolling, 0.0)
        return if (power.isNaN()) 0f else power.toFloat()
    }

    fun calculateSpeed(power: Float, speed0: Float, grade: Double): Float {
        val beta = atan(grade)
        val pGravity = speed0 * gForce * totalMass * sin(beta)
        val pDrag = speed0.pow(3) * drag
        val pRolling = speed0 * cRolling * gForce * totalMass * cos(beta)
        val finalPower = power - (pDrag + pGravity + pRolling)
        val result = (finalPower + totalMass / 2 * speed0.pow(2)) / totalMass * 2
        return sqrt(maxOf(0.0, result)).toInt().toFloat()
    }

    companion object {

        //TODO: Let the user select a bike type and use it here
        //TODO: Let the user select a tyre for his bike so cRolling does not depend on the bike
        //TODO: Make rho not final... At least to make it a a function of altitude
        private const val gForce = 9.80665
        private const val rho = 1.226
    }
}
