package com.example.projectsw.hellosmartwatch;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputWriter {

    private FileOutputStream outputStream;

    public FileOutputWriter(Context context, String fileName) {

        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File tmpFile = new File(path, fileName);

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