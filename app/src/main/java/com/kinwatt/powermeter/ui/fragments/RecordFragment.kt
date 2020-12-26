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
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.provider.RecordProvider

class RecordFragment : Fragment() {

    private var mListener: OnListFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_record_list, container, false)

        val context = view.context
        val recyclerView = view as RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context)
        recyclerView.adapter = RecordRecyclerViewAdapter(RecordProvider.getProvider(context).all, mListener)

        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            //throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Record)
    }
}
