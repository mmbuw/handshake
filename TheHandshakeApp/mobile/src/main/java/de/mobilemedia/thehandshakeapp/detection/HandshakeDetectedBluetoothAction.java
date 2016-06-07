package de.mobilemedia.thehandshakeapp.detection;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedList;

import de.mobilemedia.thehandshakeapp.mobile_core.MainFragment;

public class HandshakeDetectedBluetoothAction extends HandshakeDetectedAction {

    MainFragment mFragmentInstance;

    public HandshakeDetectedBluetoothAction(MainFragment fragmentInstance) {
        mFragmentInstance = fragmentInstance;
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
        int timestamp = getCurrentUnixTimestamp();
        String android_id = Settings.Secure.getString(mFragmentInstance.getContext().getContentResolver(),
                                                      Settings.Secure.ANDROID_ID);
        String filename = "handshake-" + timestamp;
        FileOutputWriter fileOutputWriter = new FileOutputWriter(filename);

        fileOutputWriter.writeToFile("#" + android_id + ", " + timestamp + ", " + startSample + ", " + endSample);
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


        //Show a toast to show detected handshake
        Context context = mFragmentInstance.getContext();

        if (context != null) {
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    private int getCurrentUnixTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
}
