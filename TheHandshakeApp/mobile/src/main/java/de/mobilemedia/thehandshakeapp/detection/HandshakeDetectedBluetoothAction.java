package de.mobilemedia.thehandshakeapp.detection;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.LinkedList;

import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;
import de.mobilemedia.thehandshakeapp.mobile_core.MainFragment;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;

public class HandshakeDetectedBluetoothAction extends HandshakeDetectedAction {

    MainFragment mFragmentInstance;
    Activity mParentActivity;

    public HandshakeDetectedBluetoothAction(MainFragment fragmentInstance) {
        mFragmentInstance = fragmentInstance;
        mParentActivity = mFragmentInstance.getActivity();
    }

    @Override
    public void onHandshakeDetected(LinkedList<float[]> data,
                                    int startSample,
                                    int endSample) {

        mFragmentInstance.onScanButtonClick();
        long precisionTime = System.currentTimeMillis();
        String text = "Handshake detected";
        text += "\n" + new Date().toString();
        text += "\n" + precisionTime;
        int duration = Toast.LENGTH_SHORT;

        Log.i("HDBluetoothAction", "Handshake at " + precisionTime);

        //Save handshake data to a file
        int timestamp = Util.getCurrentUnixTimestamp();
        String filename = timestamp + "-" + MainActivity.ANDROID_ID;
        FileOutputWriter fileOutputWriter = new FileOutputWriter(filename);

        fileOutputWriter.writeToFile("#" + MainActivity.ANDROID_ID + ", " +
                                     timestamp + ", " + startSample + ", " + endSample);
        for (float[] record : data) {
            if (record.length == 3) {
                fileOutputWriter.writeToFile(record[0] + ", " +
                                             record[1] + ", " +
                                             record[2]);
            }
            else if (record.length == 6) {
                fileOutputWriter.writeToFile(record[0] + ", " +
                                             record[1] + ", " +
                                             record[2] + ", " +
                                             record[3] + ", " +
                                             record[4] + ", " +
                                             record[5]);
            }
        }
        fileOutputWriter.closeStream();

        FTPUploadTask ftpu = new FTPUploadTask();
        ftpu.execute(filename);

        //Show a toast to show detected handshake
        Context context = mFragmentInstance.getContext();

        if (context != null) {
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    public class FTPUploadTask extends AsyncTask<String, Void, Void> {

        public final String LOG_TAG = FTPUploadTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... strings) {

            if (strings.length != 1) {
                return null;
            }

            File toUpload = FileOutputWriter.getFileHandleOf(strings[0]);

            FTPClient connection = new FTPClient();

            try {
                connection.connect("mmlabbuw.bplaced.net");

                if (connection.login("mmlabbuw", "igor,the_brain!")) {
                    connection.enterLocalPassiveMode();
                    connection.setFileType(FTP.BINARY_FILE_TYPE);
                    FileInputStream in = new FileInputStream(toUpload);
                    boolean result = connection.storeFile("/handshake/" + toUpload.getName(), in);
                    in.close();
                    if (result) Log.v(LOG_TAG, "Upload of " + toUpload.getName() + " succeeded.");
                    connection.logout();
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
