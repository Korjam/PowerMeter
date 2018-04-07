package com.kinwatt.powermeter.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.ui.fragments.BluetoothDeviceFragment.OnListFragmentInteractionListener;

import java.util.List;
import java.util.Map;

public class BluetoothDeviceRecyclerViewAdapter extends RecyclerView.Adapter<BluetoothDeviceRecyclerViewAdapter.BluetoothDeviceViewHolder> {

    private final Map<BluetoothDevice, List<ParcelUuid>> map;
    private final List<BluetoothDevice> items;
    private final OnListFragmentInteractionListener listener;

    public BluetoothDeviceRecyclerViewAdapter(List<BluetoothDevice> items, Map<BluetoothDevice, List<ParcelUuid>> map, OnListFragmentInteractionListener listener) {
        this.map = map;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BluetoothDeviceViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_bluetoothdevice, parent, false));
    }

    @Override
    public void onBindViewHolder(final BluetoothDeviceViewHolder holder, int position) {
        holder.setItem(items.get(position));
        holder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListFragmentInteraction(holder.getItem(), map.get(holder.getItem()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView deviceName;
        private BluetoothDevice item;

        public BluetoothDeviceViewHolder(View view) {
            super(view);
            this.view = view;
            this.deviceName = view.findViewById(R.id.bluetooth_device_name);
        }

        public BluetoothDevice getItem() {
            return item;
        }
        public void setItem(BluetoothDevice item) {
            this.item = item;
            this.deviceName.setText(item.getName());
        }

        public void setOnClickListener(View.OnClickListener listener) {
            view.setOnClickListener(listener);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + deviceName.getText() + "'";
        }
    }
}
