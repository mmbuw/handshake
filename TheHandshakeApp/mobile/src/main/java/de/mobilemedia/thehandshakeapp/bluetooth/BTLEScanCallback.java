package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.HashSet;
import java.util.List;

import de.mobilemedia.thehandshakeapp.detection.MRDFeatureExtractor;
import de.mobilemedia.thehandshakeapp.mobile_core.Config;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;


public class BTLEScanCallback extends ScanCallback {

    public static final String LOG_TAG = BTLEScanCallback.class.getSimpleName();
    public static final int DECODE_WINDOW_WIDTH = 2;

    private HashSet<Integer> receivedMessages = new HashSet<Integer>();

    @Override
    public void onScanResult(int callbackType, ScanResult result) {

        String remote = result.getDevice().getAddress();
        String local = BTLEConnectionManager.mBluetoothAdapter.getAddress();

        System.out.println(result.getRssi());

        if (remote.equals(local)) {
            return;
        }

        if (result.getRssi() < Config.BLE_MIN_RSSI) {
            Log.d(LOG_TAG, "Omitting package due to RSSI value.");
        }

        try {
            byte[] receivedBytes = result.getScanRecord().getManufacturerSpecificData(Config.BLE_TAG);

            // prevent same message to be presented multiple times
            //if (processNewMessageBytes(receivedBytes)) {
            if (true) {

                int unixTimestampOfLastShake = MRDFeatureExtractor.myLastShakeTime;
                String decodedMessage = null;

                Log.d(LOG_TAG, "IN DATA IS: ");
                Util.printByteArray(receivedBytes);

                for (int timePos = unixTimestampOfLastShake-DECODE_WINDOW_WIDTH;
                     timePos <= unixTimestampOfLastShake+DECODE_WINDOW_WIDTH;
                     ++timePos) {

                    byte[] decoded = Util.endecrypt(receivedBytes, timePos);
                    String msg = new String(decoded);
                    Log.d(LOG_TAG, "Testing key " + timePos + " with result " + msg);

                    if (msg.startsWith("!!") && msg.length() == 9) {
                        decodedMessage = msg.substring(2);
                        break;
                    }

                }

                if (decodedMessage != null) {
                    Log.d(LOG_TAG, "Received message: " + decodedMessage);
                    MainActivity.receivedHandshakes.addHandshake(new HandshakeData(decodedMessage));
                } else {
                    Log.e(LOG_TAG, "Error decoding the received message");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "No valid data using BLE_TAG");
        }
    }

    public boolean processNewMessageBytes(byte[] newMessageBytes) {

        // Compute checksum of message
        int checksum = 0;
        for (byte b : newMessageBytes) {
            checksum += b;
        }

        // Check if checksum was already seen before
        if (!receivedMessages.contains(checksum)) {
            receivedMessages.add(checksum);
            return true;
        }

        return false;

    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {

        for (ScanResult result : results) {
            onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
        }

    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e(LOG_TAG, "Scan returned error " + errorCode);
    }

}
