package com.example.projectsw.hellosmartwatch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi.*;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class MainActivity extends Activity {

    private Button mSendButton;

    private GoogleApiClient mGoogleApiClient;
    private String transcriptionNodeId = null;
    private AccelerometerReader accelerometerReader;

    private static final String TEST_MESSAGE_CAPABILITY_NAME = "test_message";
    public static final String TEST_MESSAGE_TRANSCRIPTION_PATH = "/test_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Create Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        //Create user interface
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mSendButton = (Button) stub.findViewById(R.id.sendButton);

                mSendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Void, Void, Integer>() {
                            protected Integer doInBackground(Void... params) {
                                byte randomNumber1 = (byte) (Math.random() * 100);
                                byte randomNumber2 = (byte) (Math.random() * 100);
                                byte randomNumber3 = (byte) (Math.random() * 100);
                                byte[] dataToSend = {randomNumber1, randomNumber2, randomNumber3};
                                requestTranscription(dataToSend);
                                return 0;
                            }
                        }.execute();
                    }
                });
            }
        });

        //Setup message transcription device
        new AsyncTask<Void, Void, Integer>() {
            protected Integer doInBackground(Void... params) {
                setupTestMessageTranscription();
                return 0;
            }
        }.execute();

        //Setup acceleration reader
        accelerometerReader = new AccelerometerReader(this.getApplicationContext());


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
    private void requestTranscription(byte[] data) {
        if (transcriptionNodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                            transcriptionNodeId,
                                            TEST_MESSAGE_TRANSCRIPTION_PATH,
                                            data).setResultCallback(

                    new ResultCallback<SendMessageResult>() {
                        @Override
                        public void onResult(SendMessageResult sendMessageResult) {
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
