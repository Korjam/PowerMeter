package com.kinwatt.powermeter.sensor

import android.app.Activity
import android.location.Location

import com.kinwatt.powermeter.data.Position
import com.kinwatt.powermeter.data.Record

import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class LocationProviderMock(private val activity: Activity) : LocationProvider() {

    var record: Record? = null
        set(record) {
            field = record
            positions.addAll(record!!.positions)
        }

    private var positions: MutableList<Position> = ArrayList()
    private var currentIndex = 0

    private var delay: Long = 1000
    private var timer: Timer = Timer()

    var task: Runnable = Runnable {
        val p = positions[currentIndex]
        val l = Location("GpsMock")
        l.time = p.timestamp + record!!.date!!.time
        l.longitude = p.longitude
        l.latitude = p.latitude
        l.altitude = p.altitude
        l.speed = p.speed

        onLocationChanged(l)
    }

    override fun start() {
        currentIndex = 0
        this.record!!.date = Calendar.getInstance().time
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread(task)

                currentIndex++
                if (currentIndex == positions!!.size) {
                    stop()
                }
            }
        }
        timer.schedule(timerTask, delay, delay)
    }

    override fun stop() {
        timer.cancel()
        timer = Timer()
    }
}

