package com.kinwatt.powermeter.ui.activities

import android.os.Bundle
import android.view.WindowManager

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.databinding.ActivityMainBinding
import com.kinwatt.powermeter.ui.ActivityOutdoorController
import com.kinwatt.powermeter.ui.ActivityView

class MainActivity : ActivityBase(), ActivityView {

    private lateinit var controller: ActivityOutdoorController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonStop.isEnabled = false

        binding.buttonStart.setOnClickListener {
            binding.buttonStart.isEnabled = false

            binding.duration.restart()
            controller.start()

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            binding.buttonStop.isEnabled = true
        }
        binding.buttonStop.setOnClickListener {
            binding.buttonStop.isEnabled = false

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            binding.duration.stop()
            controller.stop()

            binding.buttonStart.isEnabled = true
        }

        controller = ActivityOutdoorController(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    override fun setSpeed(speed: Float) {
        binding.speed.value = (speed * 3.6f).toDouble()
    }

    override fun setAltitude(altitude: Double) {
        binding.altitude.value = altitude
    }

    override fun setDistance(distance: Float) {
        binding.distance.value = distance.toDouble()
    }

    override fun setPowerAverage3(power: Float) {
        binding.power3s.value = power.toDouble()
    }

    override fun setPowerAverage5(power: Float) {
        binding.power5s.value = power.toDouble()
    }

    override fun setPowerAverage10(power: Float) {
        binding.power10s.value = power.toDouble()
    }
}
