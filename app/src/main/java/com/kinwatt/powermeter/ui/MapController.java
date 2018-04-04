package com.kinwatt.powermeter.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.kinwatt.powermeter.common.LocationUtils;
import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.model.CyclingIndoorPowerAlgorithm;
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm;
import com.kinwatt.powermeter.sensor.SpeedListener;
import com.kinwatt.powermeter.sensor.bluetooth.SpeedAndCadenceClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapController implements SpeedListener {

    private MapActivity activity;

    private CyclingIndoorPowerAlgorithm indoor;
    private CyclingOutdoorPowerAlgorithm outdoor;

    private SpeedAndCadenceClient speedProvider;
    private float speed;

    private Timer timer;
    private TimerTask timerTask;

    public MapController(MapActivity activity) {
        this.activity = activity;

        indoor = new CyclingIndoorPowerAlgorithm(null);
        outdoor = new CyclingOutdoorPowerAlgorithm(null);

        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("");

        speedProvider = new SpeedAndCadenceClient(activity, device);

        createTimer();
    }

    public void start() {
        speedProvider.addListener(this);
        timer.schedule(timerTask, 1000);
    }

    @Override
    public void onSpeedChanged(float speed) {
        this.speed = speed;
    }

    private void createTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            private Record track = readTrack();
            private List<Float> distances = getDistances(track);
            private float totalDistance = distances.get(distances.size() - 1);
            private float distance, speedOutdoor, lastSpeed;

            @Override
            public void run() {
                int index = findIndex(distance, distances);

                Position p1 = track.getPositions().get(index);
                Position p2 = track.getPositions().get(index + 1);

                float speed = MapController.this.speed;
                p1.setSpeed(lastSpeed);
                p2.setSpeed(speed);
                float power = indoor.calculatePower(Position.convert(p1), Position.convert(p2));

                speedOutdoor = outdoor.calculateSpeed(power, speedOutdoor, getGrade(p1, p2));

                distance += speedOutdoor;
                lastSpeed = speed;

                long time = MathUtils.interpolate(
                        distances.get(index),     p1.getTimestamp(),
                        distances.get(index + 1), p2.getTimestamp(), distance);

                Position current = LocationUtils.interpolate(p1, p2, time);

                activity.runOnUiThread(() -> activity.updateMap(current));

                if (distance >= totalDistance) {
                    timer.cancel();
                    speedProvider.removeListener(MapController.this);
                }
            }
        };
    }

    /***
     * On a sorted list, returns the index of the value
     * which is immediately below to <code>value</code>.
     * @param value
     * @param values
     * @return
     */
    private static int findIndex(float value, List<Float> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) <= value && values.get(i + 1) > value) {
                return i;
            }
        }
        return value > values.get(values.size() - 1) ? values.size() : -1;
    }

    private static double getGrade(Position p1, Position p2){
        double hDiff = p2.getAltitude() - p1.getAltitude();
        return hDiff / p2.getDistance(p1);
    }

    private static List<Float> getDistances(Record track) {
        return getDistances(track.getPositions());
    }

    private static List<Float> getDistances(List<Position> positions) {
        List<Float> res = new ArrayList<>();

        res.add(0f);

        float distance = 0;

        for (int i = 1; i < positions.size(); i++) {
            Position p1 = positions.get(i - 1);
            Position p2 = positions.get(i);
            distance += p1.getDistance(p2);
            res.add(distance);
        }

        return res;
    }

    private static Record readTrack() {
        // TODO
        throw new RuntimeException("Not implemented");
    }
}
