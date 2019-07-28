package com.kinwatt.powermeter.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.ui.fragments.RecordFragment.OnListFragmentInteractionListener

import java.text.SimpleDateFormat

class RecordRecyclerViewAdapter(private val mValues: List<Record>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<RecordRecyclerViewAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        return RecordViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_record, parent, false))
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.item = mValues[position]

        holder.setOnClickListener( View.OnClickListener {
            mListener?.onListFragmentInteraction(holder.item)
        })
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class RecordViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val deviceName: TextView = view.findViewById(R.id.record_name)

        var item: Record = Record()
            set(item) {
                field = item
                this.deviceName.text = "${item.name} - ${dateFormat.format(item.date)}"
            }

        fun setOnClickListener(listener: View.OnClickListener) {
            view.setOnClickListener(listener)
        }

        override fun toString() = "${super.toString()} '${item.name}'"
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm")
    }
}
