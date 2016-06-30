package de.mobilemedia.thehandshakeapp.detection;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class WatchListenerService extends WearableListenerService {

    public static final String ACCELEROMETER_DATA_TRANSCRIPTION_PATH = "/accelerometer_data";
    public static final String DATA_NOTIFICATION_TAG = "WatchListenerServiceOnData";
    public static final String LOG_TAG = WatchListenerService.class.getSimpleName();

    public float[] GRAVITY_START_EVENT_VALUES = {20.0f, 0.0f, 0.0f};
    public float[] GRAVITY_END_EVENT_VALUES = {-20.0f, 0.0f, 0.0f};

    private MRDFeatureExtractor mFeatureExtractor;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mFeatureExtractor = new MRDFeatureExtractor(new HandshakeDetectedBluetoothAction());
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    /* When a message is received via the data API, perform theses actions */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(ACCELEROMETER_DATA_TRANSCRIPTION_PATH)) {

            float[] receivedValues = decodeMessage(messageEvent.getData());
            //Log.d(LOG_TAG, "This is a demo log");

            if (receivedValues[0] == GRAVITY_START_EVENT_VALUES[0] &&
                    receivedValues[1] == GRAVITY_START_EVENT_VALUES[1] &&
                    receivedValues[2] == GRAVITY_START_EVENT_VALUES[2]) {

                mFeatureExtractor.startDataEvent();
                Log.d(LOG_TAG, "Mobile has detected start event");

            } else if (receivedValues[0] == GRAVITY_END_EVENT_VALUES[0] &&
                    receivedValues[1] == GRAVITY_END_EVENT_VALUES[1] &&
                    receivedValues[2] == GRAVITY_END_EVENT_VALUES[2]) {

                mFeatureExtractor.endDataEvent();

            } else {

                notifyActiviyOnValuesReceived();
                mFeatureExtractor.processDataRecord(receivedValues);
            }

        }
    }

    private void notifyActiviyOnValuesReceived() {

        Intent intent = new Intent(DATA_NOTIFICATION_TAG);
        mLocalBroadcastManager.sendBroadcast(intent);

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
