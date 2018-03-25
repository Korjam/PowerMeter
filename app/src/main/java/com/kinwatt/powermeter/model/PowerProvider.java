package com.kinwatt.powermeter.model;

import android.content.Context;
import android.location.Location;

import com.kinwatt.powermeter.common.Function;
import com.kinwatt.powermeter.common.LocationUtils;
import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.sensor.LocationListener;
import com.kinwatt.powermeter.sensor.LocationProvider;

import java.util.ArrayList;
import java.util.List;

public class PowerProvider implements LocationListener {

    private static final int INTERPOLATION_STEP = 1000; // 1s

    private long baseTime;

    private LocationProvider locationProvider;
    private Location location1, location2;
    private Buffer<Location> buffer = new Buffer<>(10);

    private PowerAlgorithm powerAlgorithm;
    protected List<PowerListener> listeners = new ArrayList<>();

    public PowerProvider(Context context, LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
        this.locationProvider.addListener(this);

        reset();
    }

    public PowerAlgorithm getPowerAlgorithm() {
        return powerAlgorithm;
    }
    public void setPowerAlgorithm(PowerAlgorithm powerAlgorithm) {
        this.powerAlgorithm = powerAlgorithm;
    }

    public void addListener(PowerListener listener) {
        listeners.add(listener);
    }
    public void removeListener(PowerListener listener) {
        listeners.remove(listener);
    }

    public void reset() {
        location1 = null;
        location2 = null;
        baseTime = 0;
        buffer.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location2 == null) {
            baseTime = location.getTime();
        }
        location = new Location(location);
        location.setTime(location.getTime() - baseTime);

        if (location.getAltitude() == 0 && location1 != null && location2 != null) {
            softenAltitude(location);
        }

        if (location2 != null) {
            interpolatePositions(location);
        }
        else {
            buffer.add(location);
        }

        location1 = location2;
        location2 = location;
    }

    protected void onPowerCalculated(long time, float power) {
        for (PowerListener listener : listeners) {
            listener.onPowerMeasured(time, power);
        }
    }

    private void softenAltitude(Location position) {
        Function<Long, Double> altitude = MathUtils.interpolate(
                location1.getTime(), location1.getAltitude(),
                location2.getTime(), location2.getAltitude());

        position.setAltitude(altitude.apply(position.getTime()));

        if (position.getSpeed() == 0) {
            Function<Long, Float> speed = MathUtils.interpolate(
                    location1.getTime(), location1.getSpeed(),
                    location2.getTime(), location2.getSpeed());
            position.setSpeed(speed.apply(position.getTime()));
        }
    }

    private void interpolatePositions(Location position) {
        Function<Long, Location> interpolation = LocationUtils.interpolate(location2, position);

        long start = buffer.last().getTime() + INTERPOLATION_STEP;
        long end = position.getTime() + (INTERPOLATION_STEP - position.getTime() % INTERPOLATION_STEP);

        for (long i = start; i <= end; i += INTERPOLATION_STEP) {
            Location interpolatedPosition = interpolation.apply(i);
            Location lastPosition = buffer.last();

            float power = getPower(lastPosition, interpolatedPosition);

            buffer.add(interpolatedPosition);

            onPowerCalculated(interpolatedPosition.getTime() + baseTime, power);
        }
    }

    private float getPower(Location l1, Location l2) {
        //TODO: Calculate degrees from 5s ago;
        /*
        if (buffer.size() >= 5) {
            Location target = buffer.peek(4);

            double hDiff = l2.getAltitude() - target.getAltitude();
            double grade = hDiff / target.distanceTo(l2);

            return powerAlgorithm.calculatePower(l1, l2, grade);
        }
        else {
            return powerAlgorithm.calculatePower(l1, l2);
        }
        */
        return powerAlgorithm.calculatePower(l1, l2);
    }

    /*
    private static long getEndTime(long base, long target) {
        while (base < target) {
            base += INTERPOLATION_STEP;
        }
        return base;
    }
    */
}
