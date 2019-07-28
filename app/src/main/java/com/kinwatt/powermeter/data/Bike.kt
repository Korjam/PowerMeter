package com.kinwatt.powermeter.data

class Bike constructor(var weight: Float = 0f, var type: BikeType = BikeType.Road)

enum class BikeType {
    Road,
    Mountain
}