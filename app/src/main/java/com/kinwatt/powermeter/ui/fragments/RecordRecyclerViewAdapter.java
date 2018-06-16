package com.kinwatt.powermeter.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.ui.fragments.RecordFragment.OnListFragmentInteractionListener;

import java.text.SimpleDateFormat;
import java.util.List;

public class RecordRecyclerViewAdapter extends RecyclerView.Adapter<RecordRecyclerViewAdapter.RecordViewHolder> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

    private final List<Record> mValues;
    private final OnListFragmentInteractionListener mListener;

    public RecordRecyclerViewAdapter(List<Record> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecordViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_record, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecordViewHolder holder, int position) {
        holder.setItem(mValues.get(position));
        holder.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onListFragmentInteraction(holder.getItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView deviceName;
        private Record item;

        public RecordViewHolder(View view) {
            super(view);
            this.view = view;
            this.deviceName = view.findViewById(R.id.record_name);
        }

        public Record getItem() {
            return item;
        }
        public void setItem(Record item) {
            this.item = item;
            this.deviceName.setText(item.getName() + " - " + dateFormat.format(item.getDate()));
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
