package com.example.projectsw.hellosmartwatch;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputWriter {

    private FileOutputStream outputStream;

    public FileOutputWriter(Context context, String fileName) {

        try {
            File tmpFile = new File(context.getExternalCacheDir() + "/" + fileName);

            try {
                outputStream = new FileOutputStream(tmpFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) { e.printStackTrace(); }

    }

    public void writeToFile(String line) {

        try {
            outputStream.write(line.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


}