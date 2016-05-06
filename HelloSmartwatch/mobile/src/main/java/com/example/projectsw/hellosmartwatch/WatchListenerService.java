package com.example.projectsw.hellosmartwatch;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WatchListenerService extends WearableListenerService {

    public static final String ACCELEROMETER_DATA_TRANSCRIPTION_PATH = "/accelerometer";

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
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("AccelerationDataAction");
        broadcastIntent.putExtra("AccelerationData", data);
        sendBroadcast(broadcastIntent);
    }

}
