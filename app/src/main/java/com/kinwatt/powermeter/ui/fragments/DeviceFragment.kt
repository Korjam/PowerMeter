package com.kinwatt.powermeter.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.SensorData
import com.kinwatt.powermeter.data.provider.SensorProvider
import com.kinwatt.powermeter.databinding.FragmentDeviceBinding

import java.util.ArrayList

class DeviceFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null

    private val deviceAdapter: DeviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(items, listener) }

    private var items: List<SensorData> = ArrayList()
    private lateinit var provider: SensorProvider
    private lateinit var binding: FragmentDeviceBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        provider = SensorProvider.getProvider(activity!!)
        items = provider.all

        val recyclerView = view as RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(view.getContext())
        recyclerView.adapter = deviceAdapter

        return view
    }

    fun add(sensorData: SensorData) {
        provider.add(sensorData)
        deviceAdapter.notifyItemInserted(items.size - 1)
    }

    operator fun contains(sensorData: SensorData): Boolean {
        return items.contains(sensorData)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            //throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(sensorData: SensorData)
    }
}
