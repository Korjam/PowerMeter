package com.kinwatt.powermeter.data.mappers

import com.kinwatt.powermeter.data.Record
import kotlinx.serialization.json.Json

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.Writer
import java.util.Scanner

object RecordMapper {

    @Throws(IOException::class)
    fun save(record: Record, filePath: String) {
        save(record, File(filePath))
    }

    @Throws(IOException::class)
    fun save(record: Record, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        }
        val writer = FileWriter(file)
        save(record, writer)
        writer.close()
    }

    @Throws(IOException::class)
    fun save(record: Record, streamWriter: Writer) {
        streamWriter.write(Json.stringify(Record.serializer(), record))
    }

    fun load(filePath: String): Record {
        return load(File(filePath))
    }

    fun load(file: File): Record {
        val reader = FileReader(file)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(stream: InputStream): Record {
        val reader = InputStreamReader(stream)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(reader: Reader): Record {
        return Json.parse(Record.serializer(), convertStreamToString(reader))
    }

    private fun convertStreamToString(reader: Reader): String {
        val s = Scanner(reader).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
