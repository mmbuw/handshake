package com.example.projectsw.hellosmartwatch;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WatchListenerService extends WearableListenerService {

    public static final String TEST_MESSAGE_TRANSCRIPTION_PATH = "/test_message";

    /* When a message is received via the data API, perform theses actions */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(TEST_MESSAGE_TRANSCRIPTION_PATH)) {
            /*Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("TEST_MESSAGE", messageEvent.getData());
            startActivity(startIntent);*/

            float[] receivedValues = decodeMessage(messageEvent.getData());

            System.out.println(receivedValues[0] + ", " +
                               receivedValues[1] + ", " +
                               receivedValues[2]);
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

}
