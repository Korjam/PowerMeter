package com.kinwatt.powermeter.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.widget.Toast

import com.kinwatt.powermeter.common.LocationUtils
import com.kinwatt.powermeter.common.mathUtils.*
import com.kinwatt.powermeter.data.Position
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.SensorData
import com.kinwatt.powermeter.data.provider.SensorProvider
import com.kinwatt.powermeter.model.CyclingIndoorPowerAlgorithm
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm
import com.kinwatt.powermeter.sensor.SpeedListener
import com.kinwatt.powermeter.sensor.bluetooth.SpeedAndCadenceClient
import com.kinwatt.powermeter.ui.activities.MapActivity

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask

class MapController(private val activity: MapActivity) : SpeedListener {

    private val indoor: CyclingIndoorPowerAlgorithm
    private val outdoor: CyclingOutdoorPowerAlgorithm

    private var speedProvider: SpeedAndCadenceClient? = null
    private var speed: Float = 0.toFloat()

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    private val track: Record
    private val distances: List<Float>

    private var running: Boolean = false

    init {

        running = false

        indoor = CyclingIndoorPowerAlgorithm()
        outdoor = CyclingOutdoorPowerAlgorithm(null)

        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }

        val provider = SensorProvider.getProvider(activity)
        val data = provider.findSensor(SpeedAndCadenceClient.SERVICE_UUID)

        if (data != null) {
            val device = bluetoothAdapter.getRemoteDevice(data.address)

            speedProvider = SpeedAndCadenceClient(activity, device)
            speedProvider!!.addListener(this)
        }

        track = readTrack("gibralfaro.txt")
        distances = getDistances(track)
    }

    fun start() {
        if (!running) {
            activity.clearMap()
            activity.updateMap(track.positions[0])
            speedProvider!!.connect()
            createTimer()
            timer!!.schedule(timerTask, 0, 1000)
            running = true
        }
    }

    fun stop() {
        if (running) {
            speedProvider!!.close()
            timer!!.cancel()
            running = false
        }
    }

    override fun onSpeedChanged(rpm: Float) {
        // Speed in m/s
        this.speed = rpm * 2.099f / 60
    }

    private fun createTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            private val totalDistance = distances[distances.size - 1]
            private var distance: Float = 0f
            private var speedOutdoor: Float = 0f
            private var lastSpeed: Float = 0f

            private val MULTIPLIER = 4f

            override fun run() {
                var index = findIndex(distance, distances)

                var p1 = track.positions[index]
                var p2 = track.positions[index + 1]

                val speed = this@MapController.speed
                p1.speed = lastSpeed
                p2.speed = speed
                val power = indoor.calculatePower(Position.convert(p1), Position.convert(p2))

                speedOutdoor = outdoor.calculateSpeed(power, speedOutdoor, getGrade(p1, p2))

                distance += speedOutdoor * MULTIPLIER
                distance = minOf(distance, totalDistance)
                lastSpeed = speed

                index = minOf(findIndex(distance, distances), distances.size - 2)
                p1 = track.positions[index]
                p2 = track.positions[index + 1]

                val time = interpolate(
                        distances[index], p1.timestamp,
                        distances[index + 1], p2.timestamp, distance)

                val current = LocationUtils.interpolate(p1, p2, time)

                activity.runOnUiThread {
                    activity.setSpeed(speedOutdoor * 3.6f)
                    activity.setPower(power)
                    activity.updateMap(current)
                    if (distance >= totalDistance) {
                        activity.stopTimer()
                    }
                }

                if (distance >= totalDistance) {
                    stop()
                }
            }
        }
    }

    /***
     * On a sorted list, returns the index of the value
     * which is immediately below to `value`.
     * @param value
     * @param values
     * @return
     */
    private fun findIndex(value: Float, values: List<Float>): Int {
        for (i in 0 until values.size - 1) {
            if (values[i] <= value && values[i + 1] > value) {
                return i
            }
        }

        return if (value >= values[values.size - 1]) values.size else -1
    }

    private fun getGrade(p1: Position, p2: Position): Double = (p2.altitude - p1.altitude) / p2.distanceTo(p1)

    private fun getDistances(track: Record): List<Float> = getDistances(track.positions)

    private fun getDistances(positions: List<Position>): List<Float> {
        val res = ArrayList<Float>()

        res.add(0f)

        var distance = 0f

        for (i in 1 until positions.size) {
            val p1 = positions[i - 1]
            val p2 = positions[i]
            distance += p1.distanceTo(p2)
            res.add(distance)
        }

        return res
    }

    private fun readTrack(filePath: String): Record {
        val track = Record()
        try {
            val br = BufferedReader(InputStreamReader(activity.assets.open(filePath)))

            var time: Long = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (strLine in br.lines()) {
                    val values = strLine.split(";".toRegex())
                            .filter { it.isNotEmpty() }
                            .toTypedArray()

                    track.positions.add(Position(
                            values[1].toDouble(),
                            values[2].toDouble(),
                            values[0].toDouble(),
                            0f,
                            time))
                    time += 1000
                }
            }

            br.close()
        } catch (e: IOException) {
            Toast.makeText(activity, "Error: " + e.message, Toast.LENGTH_LONG).show()
        }

        return track
    }
}
