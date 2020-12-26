package com.kinwatt.powermeter.ui.fragments

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.os.ParcelUuid
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.databinding.FragmentBluetoothdeviceBinding

import java.util.ArrayList
import java.util.HashMap

class BluetoothDeviceFragment : Fragment() {

    private lateinit var binding: FragmentBluetoothdeviceBinding
    private var listener: OnListFragmentInteractionListener? = null

    private val deviceAdapter: BluetoothDeviceRecyclerViewAdapter by lazy { BluetoothDeviceRecyclerViewAdapter(items, map, listener) }

    private val items = ArrayList<BluetoothDevice>()
    private val map = HashMap<BluetoothDevice, List<ParcelUuid>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBluetoothdeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        val recyclerView = view as RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = deviceAdapter

        return view
    }

    operator fun contains(device: BluetoothDevice): Boolean {
        return items.contains(device)
    }

    fun add(device: BluetoothDevice, uuids: List<ParcelUuid>) {
        items.add(device)
        map[device] = uuids
        deviceAdapter.notifyItemInserted(items.size - 1)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(device: BluetoothDevice, uuids: List<ParcelUuid>)
    }
}
