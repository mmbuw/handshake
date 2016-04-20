package com.example.projectsw.hellosmartwatch;

import android.content.Intent;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {

    public static final String TEST_MESSAGE_TRANSCRIPTION_PATH = "/test_message";

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        System.out.println("Received message");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(TEST_MESSAGE_TRANSCRIPTION_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("TEST_MESSAGE", messageEvent.getData());
            startActivity(startIntent);
            System.out.println("RECEIIIIIIIIIIVED");
        }
    }

}
