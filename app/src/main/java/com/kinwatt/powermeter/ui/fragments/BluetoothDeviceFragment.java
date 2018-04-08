package com.kinwatt.powermeter.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinwatt.powermeter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothDeviceFragment extends Fragment {

    private OnListFragmentInteractionListener listener;

    private BluetoothDeviceRecyclerViewAdapter adapter;

    private List<BluetoothDevice> items = new ArrayList<>();
    private Map<BluetoothDevice, List<ParcelUuid>> map = new HashMap<>();

    public BluetoothDeviceFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetoothdevice_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter = new BluetoothDeviceRecyclerViewAdapter(items, map, listener));

        return view;
    }

    public boolean contains(BluetoothDevice device) {
        return items.contains(device);
    }

    public void add(BluetoothDevice device, List<ParcelUuid> uuids) {
        items.add(device);
        map.put(device, uuids);
        adapter.notifyItemInserted(items.size() - 1);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(BluetoothDevice device, List<ParcelUuid> uuids);
    }
}
