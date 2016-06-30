package de.mobilemedia.thehandshakeapp.detection;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.mobilemedia.thehandshakeapp.bluetooth.BTLEConnectionManager;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;
import de.mobilemedia.thehandshakeapp.mobile_core.Config;

import static de.mobilemedia.thehandshakeapp.bluetooth.Util.loadMapFromFile;
import static de.mobilemedia.thehandshakeapp.bluetooth.Util.saveMapToFile;

public class WatchListenerService extends WearableListenerService {

    public static final String ACCELEROMETER_DATA_TRANSCRIPTION_PATH = "/accelerometer_data";
    public static final String INTENT_TO_ACTIVITY_NAME = "watchListenerToActivity";
    public static final String ACTION_EXTRA_TAG = "action";
    public static final String DATA_NOTIFICATION_ACTION = "onData";
    public static final String HANDSHAKE_NOTIFICATION_ACTION = "onHandshake";
    public static final String LOG_TAG = WatchListenerService.class.getSimpleName();

    public static File FILE_STORAGE_PATH;

    public float[] GRAVITY_START_EVENT_VALUES = {20.0f, 0.0f, 0.0f};
    public float[] GRAVITY_END_EVENT_VALUES = {-20.0f, 0.0f, 0.0f};

    private MRDFeatureExtractor mFeatureExtractor;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BTLEConnectionManager mBleConnectionManager;
    public static ReceivedHandshakes mReceivedHandshakes;

    @Override
    public void onCreate() {
        super.onCreate();
        FILE_STORAGE_PATH = this.getFilesDir();
        mFeatureExtractor = new MRDFeatureExtractor(new HandshakeDetectedBluetoothAction(this));
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBleConnectionManager = new BTLEConnectionManager(this);
        mReceivedHandshakes = new ReceivedHandshakes();
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

                notifyActivityOnValuesReceived();
                mFeatureExtractor.processDataRecord(receivedValues);
            }

        }
    }

    private void notifyActivityOnValuesReceived() {

        Intent intent = new Intent(INTENT_TO_ACTIVITY_NAME);
        intent.putExtra(ACTION_EXTRA_TAG, DATA_NOTIFICATION_ACTION);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void onHandshake() {
        mBleConnectionManager.scanBTLE(true);
        mBleConnectionManager.advertiseBTLE(true);
        MRDFeatureExtractor.myLastShakeTime = Util.getCurrentUnixTimestamp();

        //DEBUG
        processNewHandshake("1tu9Zko");
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

    public static void processNewHandshake(String decodedMessage) {
        HandshakeData hd = new HandshakeData(decodedMessage);

        loadPrevData();
        mReceivedHandshakes.addHandshake(hd);
        saveCurrentData();
    }

    public static void loadPrevData(){
        File handshakesFile = new File(FILE_STORAGE_PATH, Config.HANDSHAKE_FILE_NAME);
        Map savedHandshakes = loadMapFromFile(handshakesFile);
        if (savedHandshakes != null) {
            mReceivedHandshakes.setReceivedHandshakesMap((HashMap<String, HandshakeData>) savedHandshakes);
            Log.d("LOAD", "handshakes loaded");
        }
    }

    public static void saveCurrentData(){
        HashMap<String, HandshakeData> handshakesMap = mReceivedHandshakes.getReceivedHandshakesMap();
        File handshakesFile = new File(FILE_STORAGE_PATH, Config.HANDSHAKE_FILE_NAME);
        saveMapToFile(handshakesMap, handshakesFile);
        Log.d("SAVE", "handshakes saved");
    }

}
