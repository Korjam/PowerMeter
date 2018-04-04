package com.kinwatt.powermeter.ui;

import android.content.Context;
import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;
import com.kinwatt.powermeter.model.Buffer;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.data.mappers.RecordMapper;
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm;
import com.kinwatt.powermeter.sensor.LocationListener;
import com.kinwatt.powermeter.sensor.LocationProvider;
import com.kinwatt.powermeter.model.PowerListener;
import com.kinwatt.powermeter.model.PowerProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ActivityController implements LocationListener, PowerListener {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private Context context;
    private ActivityView view;

    private LocationProvider locationProvider;
    private Location lastLocation;
    private float distance = 0;

    private PowerProvider powerProvider;
    private Buffer<Float> powers = new Buffer<>(10);

    private Record record;

    private boolean running = false;

    public ActivityController(Context context, ActivityView view) {
        this.context = context;
        this.view = view;

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
        powerProvider.setPowerAlgorithm(new CyclingOutdoorPowerAlgorithm(null));

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
        }
    }

    public void stop() {
        if (running) {
            running = false;
            locationProvider.stop();
            powerProvider.reset();

            try {
                saveRecord(record);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
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

    private void saveRecord(Record record) throws IOException {
        RecordMapper.save(record, getFile(record));
    }

    private File getFile(Record item) {
        return new File(context.getFilesDir(), String.format("%s_%s.json", item.getName(), DATE_FORMAT.format(item.getDate())));
    }
}
