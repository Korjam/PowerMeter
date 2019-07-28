package com.kinwatt.powermeter.data.mappers

import com.kinwatt.powermeter.data.User

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.Writer
import java.util.Scanner

import kotlinx.serialization.json.Json

object UserMapper {
    @Throws(IOException::class)
    fun save(user: User, filePath: String) {
        save(user, File(filePath))
    }

    @Throws(IOException::class)
    fun save(user: User, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        }
        val writer = FileWriter(file)
        save(user, writer)
        writer.close()
    }

    @Throws(IOException::class)
    fun save(user: User, streamWriter: Writer) {
        streamWriter.write(Json.stringify(User.serializer(), user))
    }

    fun load(file: File): User {
        val reader = FileReader(file)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(stream: InputStream): User {
        val reader = InputStreamReader(stream)
        val res = load(reader)
        reader.close()
        return res
    }

    fun load(reader: Reader): User {
        return Json.parse(User.serializer(), convertStreamToString(reader))
    }

    private fun convertStreamToString(reader: Reader): String {
        val s = Scanner(reader).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
