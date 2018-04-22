package com.kinwatt.powermeter.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.data.provider.RecordProvider;
import com.kinwatt.powermeter.ui.fragments.RecordFragment;

public class RecordListActivity extends ActivityBase implements RecordFragment.OnListFragmentInteractionListener {

    private RecordFragment recordFragment;
    private TextView noDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        noDevices = findViewById(R.id.no_records);
        recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentById(R.id.records);

        FloatingActionButton fab = findViewById(R.id.add);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        if (RecordProvider.getProvider(this).getAll().isEmpty()) {
            recordFragment.getView().setVisibility(View.GONE);
        }
        else {
            noDevices.setVisibility(View.GONE);
        }
    }

    @Override
    public void onListFragmentInteraction(Record item) {
        Intent intent = new Intent(this, RecordSummaryActivity.class);
        intent.putExtra("file_name", RecordProvider.getProvider(this).getFile(item).getAbsolutePath());
        startActivity(intent);
    }
}
