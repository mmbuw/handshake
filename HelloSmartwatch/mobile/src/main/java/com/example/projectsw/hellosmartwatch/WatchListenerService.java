package com.example.projectsw.hellosmartwatch;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

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

            ByteBuffer byteBuffer = ByteBuffer.wrap(messageEvent.getData());
            System.out.println(byteBuffer.getFloat(0) + ", " +
                               byteBuffer.getFloat(1) + ", " +
                               byteBuffer.getFloat(2));
        }
    }

}
