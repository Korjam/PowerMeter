package com.kinwatt.powermeter.sensor;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public abstract class LocationProvider {
    public static final int GPS_PROVIDER = 0;
    public static final int NETWORK_PROVIDER = 1;
    public static final int FUSED_PROVIDER = 2;
    public static final int MOCK_PROVIDER = 3;

    private ArrayList<LocationListener> mListeners;

    public LocationProvider() {
        mListeners = new ArrayList<>();
    }

    public void addListener(LocationListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(LocationListener listener) {
        mListeners.remove(listener);
    }

    public static LocationProvider createProvider(Context context, int providerType)
    {
        switch (providerType) {
            case GPS_PROVIDER:
                return new GpsLocationProvider(context);
            case NETWORK_PROVIDER:
                return new NetworkLocationProvider(context);
            case FUSED_PROVIDER:
                return new FusedLocationProvider(context);
            case MOCK_PROVIDER:
                return new LocationProviderMock((Activity) context);
        }
        throw new RuntimeException();
    }

    public abstract void start();
    public abstract void stop();

    protected void onLocationChanged(Location location) {
        for (LocationListener listener : mListeners) {
            listener.onLocationChanged(location);
        }
    }
}

class SystemLocationProvider extends LocationProvider {

    private android.location.LocationListener locationListener;
    private LocationManager locationManager;
    private boolean listening = false;
    private String mProvider;

    public SystemLocationProvider(Context context, String provider) {
        mProvider = provider;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                SystemLocationProvider.this.onLocationChanged(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void start() {
        if (!listening) {
            listening = true;
            locationManager.requestLocationUpdates(mProvider, 0, 0, locationListener);
        }
    }

    @Override
    public void stop() {
        if (listening) {
            locationManager.removeUpdates(locationListener);
            listening = false;
        }
    }
}

class GpsLocationProvider  extends SystemLocationProvider {
    public GpsLocationProvider(Context context) {
        super(context, LocationManager.GPS_PROVIDER);
    }
}

class NetworkLocationProvider  extends SystemLocationProvider {
    public NetworkLocationProvider(Context context) {
        super(context, LocationManager.NETWORK_PROVIDER);
    }
}

class FusedLocationProvider extends LocationProvider {
    private static final int INTERVAL = 5000;

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean listening = false;

    public FusedLocationProvider(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    FusedLocationProvider.this.onLocationChanged(location);
                }
            }
        };
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void start() {
        if (!listening) {
            listening = true;
            mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,null);
        }
    }

    @Override
    public void stop() {
        if (listening) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            listening = false;
        }
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest().setInterval(INTERVAL).setFastestInterval(INTERVAL).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}