package com.kinwatt.powermeter.ui.activities

import android.os.Bundle
import android.view.WindowManager

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.ui.ActivityOutdoorController
import com.kinwatt.powermeter.ui.ActivityView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ActivityBase(), ActivityView {

    private lateinit var controller: ActivityOutdoorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        buttonStop.isEnabled = false

        buttonStart.setOnClickListener {
            buttonStart.isEnabled = false

            duration.restart()
            controller.start()

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            buttonStop.isEnabled = true
        }
        buttonStop.setOnClickListener {
            buttonStop.isEnabled = false

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            duration.stop()
            controller.stop()

            buttonStart.isEnabled = true
        }

        controller = ActivityOutdoorController(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    override fun setSpeed(speed: Float) {
        this.speed.value = (speed * 3.6f).toDouble()
    }

    override fun setAltitude(altitude: Double) {
        this.altitude.value = altitude
    }

    override fun setDistance(distance: Float) {
        this.distance.value = distance.toDouble()
    }

    override fun setPowerAverage3(power: Float) {
        this.power3s.value = power.toDouble()
    }

    override fun setPowerAverage5(power: Float) {
        this.power5s.value = power.toDouble()
    }

    override fun setPowerAverage10(power: Float) {
        this.power10s.value = power.toDouble()
    }
}
