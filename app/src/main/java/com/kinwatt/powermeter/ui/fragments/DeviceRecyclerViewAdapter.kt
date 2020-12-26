package com.kinwatt.powermeter.ui.fragments

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.SensorData
import com.kinwatt.powermeter.ui.fragments.DeviceFragment.OnListFragmentInteractionListener

class DeviceRecyclerViewAdapter(
    private val items: List<SensorData>,
    private val listener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_device, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.item = items[position]
        holder.setOnClickListener( View.OnClickListener {
            listener?.onListFragmentInteraction(holder.item!!)
        } )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class DeviceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val deviceName: TextView = view.findViewById(R.id.device_name)

        var item: SensorData = SensorData()
            set(item) {
                field = item
                this.deviceName.text = item.name
            }

        fun setOnClickListener(listener: View.OnClickListener) {
            view.setOnClickListener(listener)
        }

        override fun toString() = "${super.toString()} '${item.name}'"
    }
}
