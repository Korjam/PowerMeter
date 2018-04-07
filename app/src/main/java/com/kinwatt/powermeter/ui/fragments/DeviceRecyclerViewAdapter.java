package com.kinwatt.powermeter.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.SensorData;
import com.kinwatt.powermeter.ui.fragments.DeviceFragment.OnListFragmentInteractionListener;

import java.util.List;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.DeviceViewHolder> {

    private final List<SensorData> items;
    private final OnListFragmentInteractionListener listener;

    public DeviceRecyclerViewAdapter(List<SensorData> items, OnListFragmentInteractionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DeviceViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_device, parent, false));
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, int position) {
        holder.setItem(items.get(position));
        holder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListFragmentInteraction(holder.getItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView deviceName;
        private SensorData item;

        public DeviceViewHolder(View view) {
            super(view);
            this.view = view;
            this.deviceName = view.findViewById(R.id.device_name);
        }

        public SensorData getItem() {
            return item;
        }
        public void setItem(SensorData item) {
            this.item = item;
            this.deviceName.setText(item.getName());
        }

        public void setOnClickListener(View.OnClickListener listener) {
            view.setOnClickListener(listener);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item.getName() + "'";
        }
    }
}
