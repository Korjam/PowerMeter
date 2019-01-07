package com.kinwatt.powermeter.model

import android.location.Location

import com.kinwatt.powermeter.data.User

interface PowerAlgorithm {

    fun calculatePower(pos1: Location, pos2: Location): Float

    fun calculatePower(pos1: Location, pos2: Location, grade: Double): Float
}
