package com.kinwatt.powermeter.ui;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.SensorData;
import com.kinwatt.powermeter.data.ServiceData;
import com.kinwatt.powermeter.data.provider.SensorProvider;
import com.kinwatt.powermeter.ui.fragments.BluetoothDeviceFragment;
import com.kinwatt.powermeter.ui.fragments.ConnectDeviceDialogFragment;
import com.kinwatt.powermeter.ui.fragments.DeviceFragment;

import java.util.List;

public class DevicesActivity extends AppCompatActivity implements BluetoothDeviceFragment.OnListFragmentInteractionListener {

    private DeviceFragment deviceFragment;

    private TextView noDevices;

    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        noDevices = findViewById(R.id.no_devices);

        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(v -> {
            ConnectDeviceDialogFragment dialogFragment = new ConnectDeviceDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "connect_devices_dialog");
        });

        deviceFragment = (DeviceFragment) getSupportFragmentManager().findFragmentById(R.id.connected_devices);

        if (SensorProvider.getProvider(this).getAll().isEmpty()) {
            deviceFragment.getView().setVisibility(View.GONE);
        }
        else {
            noDevices.setVisibility(View.GONE);
        }
    }

    @Override
    public void onListFragmentInteraction(BluetoothDevice device, List<ParcelUuid> uuids) {
        SensorData data = convert(device, uuids);
        if (!deviceFragment.contains(data)) {
            deviceFragment.add(data);

            deviceFragment.getView().setVisibility(View.VISIBLE);
            noDevices.setVisibility(View.GONE);
        }
    }

    private static SensorData convert(BluetoothDevice device, List<ParcelUuid> uuids) {
        SensorData sensor = new SensorData();
        sensor.setName(device.getName());
        sensor.setAddress(device.getAddress());

        for (ParcelUuid uuid : uuids) {
            sensor.getServices().add(new ServiceData(uuid.getUuid()));
        }

        return sensor;
    }
}
