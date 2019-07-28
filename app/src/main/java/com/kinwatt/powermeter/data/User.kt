package com.kinwatt.powermeter.data

import java.util.ArrayList

class User(var name: String = "", var age: Int = 0, var weight: Float = 0f, var height: Int = 0) {

    val bikes: MutableList<Bike> = ArrayList()
}