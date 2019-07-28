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
import com.kinwatt.powermeter.ui.MapController

import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : ActivityBase(), OnMapReadyCallback {

    private lateinit var options: PolylineOptions
    private var mMap: GoogleMap? = null
    private var line: Polyline? = null

    private lateinit var controller: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        buttonStop.isEnabled = false

        speed.units = "km/h"
        power.units = "W"

        buttonStart.setOnClickListener { v ->
            buttonStart.isEnabled = false

            duration.restart()
            controller.start()

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            buttonStop.isEnabled = true
        }

        buttonStop.setOnClickListener { v ->
            buttonStop.isEnabled = false

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            duration.stop()
            controller.stop()

            buttonStart.isEnabled = true
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
        duration.stop()
        buttonStop.isEnabled = false
        buttonStart.isEnabled = true
    }

    fun setSpeed(speed: Float) {
        this.speed.value = speed.toDouble()
    }

    fun setPower(power: Float) {
        this.power.value = power.toDouble()
    }

    fun clearMap() {
        if (mMap != null) {
            mMap!!.clear()
            options.points.clear()
        }
    }
}
