package com.kinwatt.powermeter.ui.activities

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.os.Bundle
import android.view.View

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.provider.RecordProvider
import com.kinwatt.powermeter.ui.fragments.RecordFragment

import kotlinx.android.synthetic.main.activity_record_list.*

class RecordListActivity : ActivityBase(), RecordFragment.OnListFragmentInteractionListener {

    private lateinit var recordFragment: RecordFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_list)

        recordFragment = supportFragmentManager.findFragmentById(R.id.records) as RecordFragment

        val fab = findViewById<FloatingActionButton>(R.id.add)
        fab.setOnClickListener { view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        if (RecordProvider.getProvider(this).all.isEmpty()) {
            recordFragment.view!!.visibility = View.GONE
        } else {
            noRecords.visibility = View.GONE
        }
    }

    override fun onListFragmentInteraction(item: Record) {
        val intent = Intent(this, RecordSummaryActivity::class.java)
        intent.putExtra("file_name", RecordProvider.getProvider(this).getFile(item).absolutePath)
        startActivity(intent)
    }
}
