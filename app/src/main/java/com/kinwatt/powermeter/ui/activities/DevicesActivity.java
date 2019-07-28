package com.kinwatt.powermeter.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private BluetoothAdapter bluetoothAdapter;

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
        }

        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        connectButton.setEnabled(bluetoothLeScanner != null);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (bluetoothState == BluetoothAdapter.STATE_ON) {
                    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                    connectButton.setEnabled(bluetoothLeScanner != null);
                    unregisterReceiver(this);
                }
            }
        }
    };
}
