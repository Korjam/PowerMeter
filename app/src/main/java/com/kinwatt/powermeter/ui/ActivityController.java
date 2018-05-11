package com.kinwatt.powermeter.ui;

import android.content.Context;
import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.data.User;
import com.kinwatt.powermeter.data.mappers.UserMapper;
import com.kinwatt.powermeter.data.provider.RecordProvider;
import com.kinwatt.powermeter.model.Buffer;
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm;
import com.kinwatt.powermeter.model.PowerListener;
import com.kinwatt.powermeter.model.PowerProvider;
import com.kinwatt.powermeter.sensor.LocationListener;
import com.kinwatt.powermeter.sensor.LocationProvider;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ActivityController implements LocationListener, PowerListener {

    private Context context;
    private ActivityView view;

    private LocationProvider locationProvider;
    private Location lastLocation;
    private float distance = 0;

    private PowerProvider powerProvider;
    private Buffer<Float> powers = new Buffer<>(10);

    private RecordProvider recordProvider;
    private Record record;

    private User user;

    private boolean running = false;

    public ActivityController(Context context, ActivityView view) {
        this.context = context;
        this.view = view;

        recordProvider = RecordProvider.getProvider(context);

        locationProvider = LocationProvider.createProvider(context, LocationProvider.FUSED_PROVIDER);
        /*
        // DEBUG PURPOSES
        locationProvider = LocationProvider.createProvider(context, LocationProvider.MOCK_PROVIDER);
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Cycling outdoor_20180312_085247.json");
            ((LocationProviderMock)locationProvider).setRecord(RecordMapper.load(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        powerProvider = new PowerProvider(context, locationProvider);

        try {
            user = UserMapper.load(new File(context.getFilesDir(), "user_data.json"));
        } catch (IOException e) {
            user = null;
            e.printStackTrace();
        }
        powerProvider.setPowerAlgorithm(new CyclingOutdoorPowerAlgorithm(user));

        /*
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        */

        locationProvider.addListener(this);
        powerProvider.addListener(this);
    }

    public void start() {
        if (!running) {
            running = true;
            locationProvider.start();
            powerProvider.reset();
            record = new Record();
            record.setName("Cycling outdoor");
            record.setDate(new Date());
        }
    }

    public void stop() {
        if (running) {
            running = false;
            locationProvider.stop();
            powerProvider.reset();

            recordProvider.add(record);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        record.addPosition(location);

        view.setSpeed(location.getSpeed());
        view.setAltitude(location.getAltitude());

        if (lastLocation != null) {
            distance += lastLocation.distanceTo(location);
            view.setDistance(distance);
        }

        lastLocation = location;
    }

    @Override
    public void onPowerMeasured(long eventTime, float power) {
        powers.add(power);
        if (powers.size() >= 3) {
            view.setPowerAverage3(MathUtils.average(powers.last(3)));

            if (powers.size() >= 5) {
                view.setPowerAverage5(MathUtils.average(powers.last(5)));

                if (powers.size() >= 10) {
                    view.setPowerAverage10(MathUtils.average(powers.last(10)));
                }
            }
        }
    }
}
