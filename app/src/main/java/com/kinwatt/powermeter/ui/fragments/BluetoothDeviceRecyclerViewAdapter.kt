package com.kinwatt.powermeter.ui.fragments

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.ui.fragments.BluetoothDeviceFragment.OnListFragmentInteractionListener

class BluetoothDeviceRecyclerViewAdapter(
    private val items: List<BluetoothDevice>,
    private val map: Map<BluetoothDevice, List<ParcelUuid>>,
    private val listener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<BluetoothDeviceRecyclerViewAdapter.BluetoothDeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bluetoothdevice, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.item = items[position]
        holder.setOnClickListener( View.OnClickListener {
            listener?.onListFragmentInteraction(holder.item!!, map[holder.item]!!)
        } )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BluetoothDeviceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val deviceName: TextView = view.findViewById(R.id.bluetooth_device_name)

        var item: BluetoothDevice? = null
            set(item) {
                field = item
                deviceName.text = "${item!!.name}"
            }

        fun setOnClickListener(listener: View.OnClickListener) {
            view.setOnClickListener(listener)
        }

        override fun toString() = "${super.toString()} '${deviceName.text}'"
    }
}
