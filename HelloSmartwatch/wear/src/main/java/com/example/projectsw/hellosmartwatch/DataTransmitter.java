package com.example.projectsw.hellosmartwatch;


import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class DataTransmitter {

    public static final String TEST_MESSAGE_CAPABILITY_NAME = "test_message";
    public static final String TEST_MESSAGE_TRANSCRIPTION_PATH = "/test_message";

    private GoogleApiClient mGoogleApiClient;
    private String transcriptionNodeId = null;

    public DataTransmitter(Context context) {
        //Create Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        //Setup message transcription device
        new AsyncTask<Void, Void, Integer>() {
            protected Integer doInBackground(Void... params) {
                setupTestMessageTranscription();
                return 0;
            }
        }.execute();
    }

    /* Searches for connected handheld devices that have subscribed to a message type */
    private void setupTestMessageTranscription() {
        CapabilityApi.GetCapabilityResult result =
                Wearable.CapabilityApi.getCapability(mGoogleApiClient,
                        TEST_MESSAGE_CAPABILITY_NAME,
                        CapabilityApi.FILTER_REACHABLE).await();
        updateTranscriptionCapability(result.getCapability());

        CapabilityApi.CapabilityListener capabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateTranscriptionCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                TEST_MESSAGE_CAPABILITY_NAME);

    }

    /* Called whenever the number of connected nodes changes */
    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

    /* Picks the closes device to send the message to */
    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;

        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }

        return bestNodeId;
    }

    /* Sends the given message to the best node determined before */
    public void requestTranscription(byte[] data) {
        if (transcriptionNodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                    transcriptionNodeId,
                    TEST_MESSAGE_TRANSCRIPTION_PATH,
                    data).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                System.err.println("Error: Failed to send message.");
                            } else {
                                System.out.println("Message was sucessfully sent.");
                            }
                        }
                    }
            );
        } else {
            System.err.println("Error: unable to retrieve a node with transcription capability.");
        }
    }
}
