package com.kinwatt.powermeter.sensor

import android.location.Location

interface LocationListener {
    fun onLocationChanged(location: Location)
}
