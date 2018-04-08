package com.kinwatt.powermeter.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.SensorData;
import com.kinwatt.powermeter.data.provider.SensorProvider;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends Fragment {

    private OnListFragmentInteractionListener listener;

    private DeviceRecyclerViewAdapter adapter;

    private List<SensorData> items = new ArrayList<>();
    private SensorProvider provider;

    public DeviceFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        provider = SensorProvider.getProvider(getActivity());
        items = provider.getAll();

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter = new DeviceRecyclerViewAdapter(items, listener));

        return view;
    }

    public void add(SensorData sensorData) {
        provider.add(sensorData);
        adapter.notifyItemInserted(items.size() - 1);
    }

    public boolean contains(SensorData sensorData) {
        return items.contains(sensorData);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(SensorData sensorData);
    }
}
