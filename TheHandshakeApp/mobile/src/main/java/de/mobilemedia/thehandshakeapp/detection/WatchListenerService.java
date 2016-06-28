package de.mobilemedia.thehandshakeapp.detection;

import android.content.Intent;
import android.util.Log;


import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class WatchListenerService extends WearableListenerService {

    public static final String ACCELEROMETER_DATA_TRANSCRIPTION_PATH = "/accelerometer_data";

    /* When a message is received via the data API, perform theses actions */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(ACCELEROMETER_DATA_TRANSCRIPTION_PATH)) {

            float[] receivedValues = decodeMessage(messageEvent.getData());
            sendDataToActivity(receivedValues);

        }
    }

    private float[] decodeMessage(byte[] messageData) {
        ByteArrayInputStream bas = new ByteArrayInputStream(messageData);
        DataInputStream dis = new DataInputStream(bas);
        float[] receivedValues = new float[messageData.length / 4];

        try {
            for (int i = 0; i < receivedValues.length; ++i)
                receivedValues[i] = dis.readFloat();
        } catch (IOException ioe) {}

        return receivedValues;
    }

    private void sendDataToActivity(float[] data) {

        if (!MainActivity.isOpen) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("accelerationAction");
        broadcastIntent.putExtra("acceleration", data);
        sendBroadcast(broadcastIntent);


    }

}
