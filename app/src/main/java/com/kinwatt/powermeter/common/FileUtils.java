package com.kinwatt.powermeter.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtils {

    public static void copyFile(String inputPath, String inputFile, String outputPath) throws IOException {
        //create output directory if it doesn't exist
        File dir = new File (outputPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        InputStream in = new FileInputStream(new File(inputPath, inputFile));
        OutputStream out = new FileOutputStream(new File(outputPath, inputFile));

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();

        // write the output file (You have now copied the file)
        out.flush();
        out.close();
    }
}
