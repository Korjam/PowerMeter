package com.kinwatt.powermeter.ui.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.sensor.bluetooth.SpeedAndCadenceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectDeviceDialogFragment extends DialogFragment {

    private static final String TAG = "BluetoothSearch";

    private static View view;

    private BluetoothDeviceFragment fragment;

    private BluetoothLeScanner bluetoothLeScanner;
    private Handler handler;

    private Button cancelButton;
    private Button startStopButton;

    private TextView title;

    private ProgressBar progressBar;

    private boolean searching = false;

    public ConnectDeviceDialogFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_connect_device, container, false);
        } catch (InflateException e) {
            // View already created
        }
        return view;
        //return inflater.inflate(R.layout.fragment_connect_device, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.search_title);

        cancelButton = view.findViewById(R.id.button_cancel);
        startStopButton = view.findViewById(R.id.button_start_stop);

        startStopButton.setOnClickListener(v -> {
            if (searching) {
                stopScan();
            }
            else {
                startScan();
            }
        });
        cancelButton.setOnClickListener(v -> dismiss());

        progressBar = view.findViewById(R.id.bluetooth_progress_bar);

        progressBar.setVisibility(View.GONE);

        fragment = (BluetoothDeviceFragment) getFragmentManager().findFragmentById(R.id.search_results);
        startScan();
    }

    public void startScan() {
        startScan(SpeedAndCadenceClient.SERVICE_UUID);
    }

    public void startScan(UUID serviceId) {
        startScan(new UUID[] { serviceId });
    }

    public void startScan(UUID... serviceIds) {
        List<UUID> ids = new ArrayList<>();
        for (UUID id : serviceIds) {
            ids.add(id);
        }
        startScan(ids);
    }

    public void startScan(Iterable<UUID> serviceIds) {
        if (!searching) {
            List<ScanFilter> filters = new ArrayList<>();

            ScanFilter.Builder builder = new ScanFilter.Builder();
            for (UUID uuid: serviceIds) {
                builder.setServiceUuid(new ParcelUuid(uuid));
                filters.add(builder.build());
            }

            bluetoothLeScanner.startScan(filters, new ScanSettings.Builder().build(), scanCallback);

            Log.i(TAG, "LE Search Started.");

            searching = true;

            progressBar.setVisibility(View.VISIBLE);

            startStopButton.setText(R.string.stop);

            title.setText(R.string.searching_devices);

            handler.postDelayed(() -> {
                if (searching) {
                    stopScan();
                }
            }, 15000);
        }
    }

    public void stopScan() {
        if (searching) {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.i(TAG, "LE Search Stopped.");
            searching = false;

            progressBar.setVisibility(View.GONE);

            startStopButton.setText(R.string.scan);

            title.setText(R.string.search_results);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopScan();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (searching && !fragment.contains(device)) {
                Log.i(TAG, String.format("BluetoothDevice founded: %s %s.", device.getAddress(), device.getName()));
                fragment.add(device, result.getScanRecord().getServiceUuids());
            }
        }
    };
}
