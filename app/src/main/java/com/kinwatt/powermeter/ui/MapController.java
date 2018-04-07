package com.kinwatt.powermeter.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.widget.Toast;

import com.kinwatt.powermeter.common.LocationUtils;
import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.model.CyclingIndoorPowerAlgorithm;
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm;
import com.kinwatt.powermeter.sensor.SpeedListener;
import com.kinwatt.powermeter.sensor.bluetooth.SpeedAndCadenceClient;

import java.io.BufferedReader;
import java.io.IOException;
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

    private Record track;
    private List<Float> distances;

    public MapController(MapActivity activity) {
        this.activity = activity;

        indoor = new CyclingIndoorPowerAlgorithm(null);
        outdoor = new CyclingOutdoorPowerAlgorithm(null);

        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        //TODO: Allow select the device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("E2:4D:0F:B1:77:F4");

        speedProvider = new SpeedAndCadenceClient(activity, device);
        speedProvider.addListener(this);

        track = readTrack("gibralfaro.txt");
        distances = getDistances(track);
    }

    public void start() {
        activity.clearMap();
        speedProvider.connect();
        createTimer();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void stop() {
        speedProvider.close();
        timer.cancel();
    }

    @Override
    public void onSpeedChanged(float rpm) {
        // Speed in m/s
        this.speed = rpm * 2.099f / 60;
    }

    private void createTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            private float totalDistance = distances.get(distances.size() - 1);
            private float distance, speedOutdoor, lastSpeed;

            private static final float MULTIPLIER = 4;

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

                distance += speedOutdoor * MULTIPLIER;
                distance = Math.min(distance, totalDistance);
                lastSpeed = speed;

                index = findIndex(distance, distances);
                index = Math.min(index, distances.size() - 2);
                p1 = track.getPositions().get(index);
                p2 = track.getPositions().get(index + 1);

                long time = MathUtils.interpolate(
                        distances.get(index),     p1.getTimestamp(),
                        distances.get(index + 1), p2.getTimestamp(), distance);

                Position current = LocationUtils.interpolate(p1, p2, time);

                activity.runOnUiThread(() ->  {
                    activity.setSpeed(speedOutdoor * 3.6f);
                    activity.setPower(power);
                    activity.updateMap(current);
                });

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
        return value >= values.get(values.size() - 1) ? values.size() : -1;
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

    private Record readTrack(String filePath) {
        Record track = new Record();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(activity.getAssets().open(filePath)));

            String strLine;
            long time = 0;
            while ((strLine = br.readLine()) != null)   {
                String [] values = strLine.split(";");

                Position p = new Position(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[0]));
                p.setTimestamp(time);
                track.getPositions().add(p);
                time += 1000;
            }
            br.close();
        } catch (IOException e) {
            Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return track;
    }
}
