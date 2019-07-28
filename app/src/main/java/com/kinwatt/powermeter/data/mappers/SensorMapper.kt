package com.kinwatt.powermeter.data.mappers

import com.kinwatt.powermeter.data.SensorData
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.Writer
import java.util.Scanner

object SensorMapper {

    @Throws(IOException::class)
    fun save(sensors: List<SensorData>, filePath: String) {
        save(sensors, File(filePath))
    }

    @Throws(IOException::class)
    fun save(sensors: List<SensorData>, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        }
        val writer = FileWriter(file)
        save(sensors, writer)
        writer.close()
    }

    @Throws(IOException::class)
    fun save(sensors: List<SensorData>, streamWriter: Writer) {
        streamWriter.write(Json.stringify(SensorData.serializer().list, sensors))
    }

    fun load(filePath: String): List<SensorData> {
        return load(File(filePath))
    }

    fun load(file: File): List<SensorData> {
        val reader = FileReader(file)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(stream: InputStream): List<SensorData> {
        val reader = InputStreamReader(stream)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(reader: Reader): List<SensorData> {
        return Json.parse(SensorData.serializer().list, convertStreamToString(reader))
    }

    private fun convertStreamToString(reader: Reader): String {
        val s = Scanner(reader).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}

