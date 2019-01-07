package com.kinwatt.powermeter.common

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {

    @Throws(IOException::class)
    fun copyFile(inputPath: String, inputFile: String, outputPath: String) {
        //create output directory if it doesn't exist
        val dir = File(outputPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val input = FileInputStream(File(inputPath, inputFile))
        val output = FileOutputStream(File(outputPath, inputFile))

        val buffer = ByteArray(1024)
        var read: Int
        do {
            read = input.read(buffer)
            output.write(buffer, 0, read)
        } while (read != -1)
        input.close()

        // write the output file (You have now copied the file)
        output.flush()
        output.close()
    }
}
