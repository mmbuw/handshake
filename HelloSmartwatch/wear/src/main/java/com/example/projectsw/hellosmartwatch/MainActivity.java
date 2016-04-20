package com.example.projectsw.hellosmartwatch;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi.*;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class MainActivity extends Activity {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private String transcriptionNodeId = null;

    private static final String TEST_MESSAGE_CAPABILITY_NAME = "test_message";
    public static final String TEST_MESSAGE_TRANSCRIPTION_PATH = "/test_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Wearable.API)
                            .build();
        mGoogleApiClient.connect();

        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        new AsyncTask<Void, Void, Integer>() {
            protected Integer doInBackground(Void... params) {
                setupTestMessageTranscription();
                byte[] dataToSend = {1, 2, 3};
                requestTranscription(dataToSend);
                return 0;
            }
        }.execute();

    }

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

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

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
