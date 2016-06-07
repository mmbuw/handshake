package de.mobilemedia.thehandshakeapp.detection;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputWriter {

    public static String filePostfix = "";
    private FileOutputStream outputStream;

    public FileOutputWriter(String fileName) {

        String extendedFileName = fileName;
        if (!filePostfix.isEmpty())
         extendedFileName = fileName + "-" + filePostfix;
        extendedFileName += ".txt";

        System.out.println(extendedFileName);

        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File tmpFile = new File(path, extendedFileName);

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

    public void closeStream() {
        try {
            outputStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


}