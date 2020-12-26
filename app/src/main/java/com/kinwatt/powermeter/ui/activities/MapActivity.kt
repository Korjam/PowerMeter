package com.kinwatt.powermeter.ui.activities

import android.os.Bundle
import android.view.WindowManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.Position
import com.kinwatt.powermeter.databinding.ActivityMapBinding
import com.kinwatt.powermeter.ui.MapController

class MapActivity : ActivityBase(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding

    private lateinit var options: PolylineOptions
    private var mMap: GoogleMap? = null
    private var line: Polyline? = null

    private lateinit var controller: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonStop.isEnabled = false

        binding.speed.units = "km/h"
        binding.power.units = "W"

        binding.buttonStart.setOnClickListener { v ->
            binding.buttonStart.isEnabled = false

            binding.duration.restart()
            controller.start()

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            binding.buttonStop.isEnabled = true
        }

        binding.buttonStop.setOnClickListener { v ->
            binding.buttonStop.isEnabled = false

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            binding.duration.stop()
            controller.stop()

            binding.buttonStart.isEnabled = true
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        controller = MapController(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        val malaga = LatLng(36.7161622, -4.4233658)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(malaga))
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15f))

        options = PolylineOptions()
            .width(15f)
            .color(resources.getColor(R.color.colorPrimary))
            .geodesic(true)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
    }

    fun updateMap(position: Position) {
        if (mMap != null) {
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(LatLng(position.latitude, position.longitude)))

            val point = LatLng(position.latitude, position.longitude)

            if (!options.points.contains(point)) {
                options.add(point)
            }

            val newLine = mMap!!.addPolyline(options)
            if (line != null) {
                line!!.remove()
            }
            line = newLine
        }
    }

    fun stopTimer() {
        binding.duration.stop()
        binding.buttonStop.isEnabled = false
        binding.buttonStart.isEnabled = true
    }

    fun setSpeed(speed: Float) {
        binding.speed.value = speed.toDouble()
    }

    fun setPower(power: Float) {
        binding.power.value = power.toDouble()
    }

    fun clearMap() {
        if (mMap != null) {
            mMap!!.clear()
            options.points.clear()
        }
    }
}
