package com.kinwatt.powermeter.sensor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import java.util.ArrayList

abstract class LocationProvider {

    private val mListeners: ArrayList<LocationListener> = ArrayList()

    fun addListener(listener: LocationListener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: LocationListener) {
        mListeners.remove(listener)
    }

    abstract fun start()
    abstract fun stop()

    protected fun onLocationChanged(location: Location) {
        for (listener in mListeners) {
            listener.onLocationChanged(location)
        }
    }

    companion object {
        const val GPS_PROVIDER = 0
        const val NETWORK_PROVIDER = 1
        const val FUSED_PROVIDER = 2
        const val MOCK_PROVIDER = 3

        fun createProvider(context: Context, providerType: Int): LocationProvider {
            return when (providerType) {
                GPS_PROVIDER -> GpsLocationProvider(context)
                NETWORK_PROVIDER -> NetworkLocationProvider(context)
                FUSED_PROVIDER -> FusedLocationProvider(context)
                MOCK_PROVIDER -> LocationProviderMock(context as Activity)
                else -> throw RuntimeException()
            }
        }
    }
}

internal open class SystemLocationProvider(context: Context, private val mProvider: String) : LocationProvider() {

    private val locationListener: android.location.LocationListener
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var listening = false

    init {

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                this@SystemLocationProvider.onLocationChanged(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        if (!listening) {
            listening = true
            locationManager.requestLocationUpdates(mProvider, 0, 0f, locationListener)
        }
    }

    override fun stop() {
        if (listening) {
            locationManager.removeUpdates(locationListener)
            listening = false
        }
    }
}

internal class GpsLocationProvider(context: Context) : SystemLocationProvider(context, LocationManager.GPS_PROVIDER)

internal class NetworkLocationProvider(context: Context) : SystemLocationProvider(context, LocationManager.NETWORK_PROVIDER)

internal class FusedLocationProvider(context: Context) : LocationProvider() {

    private val mLocationCallback: LocationCallback
    private val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var listening = false

    init {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                for (location in locationResult!!.locations) {
                    this@FusedLocationProvider.onLocationChanged(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        if (!listening) {
            listening = true
            mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null)
        }
    }

    override fun stop() {
        if (listening) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            listening = false
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest().setInterval(INTERVAL.toLong()).setFastestInterval(INTERVAL.toLong()).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    companion object {
        private const val INTERVAL = 5000
    }
}