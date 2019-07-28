package com.kinwatt.powermeter.data.provider

import android.content.Context
import android.os.Build

import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.mappers.RecordMapper

import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class RecordProvider private constructor(context: Context) {
    private val filesDir: File = context.filesDir

    private val records: MutableList<Record> = readRecords().toMutableList()

    val all: List<Record> get() = records

    private fun readRecords(): List<Record> {
        val records = ArrayList<Record>()

        val name = "Cycling outdoor_"

        for (file in filesDir.listFiles { pathname -> pathname.name.startsWith(name) && pathname.extension == "json" }) {
            val datetime = file.name.substring(name.length, file.name.indexOf('.'))

            var date = Date()

            try {
                date = DATE_FORMAT.parse(datetime)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            records.add(Record("Cycling outdoor", date))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            records.sortBy { it.date }
        }

        return records
    }

    fun add(record: Record) {
        try {
            RecordMapper.save(record, getFile(record))
            records.add(record)
        } catch (e: IOException) {
            throw RuntimeException(e.message, e)
        }
    }

    fun getFile(item: Record): File = File(filesDir, "${item.name}_${DATE_FORMAT.format(item.date)}.json")

    companion object {

        private var instance: RecordProvider? = null

        private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

        fun getProvider(context: Context): RecordProvider {
            if (instance == null) {
                instance = RecordProvider(context)
            }
            return instance!!
        }
    }
}
