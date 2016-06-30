package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.HashSet;
import java.util.List;

import de.mobilemedia.thehandshakeapp.detection.MRDFeatureExtractor;
import de.mobilemedia.thehandshakeapp.detection.WatchListenerService;
import de.mobilemedia.thehandshakeapp.mobile_core.Config;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;


public class BTLEScanCallback extends ScanCallback {

    public static final String LOG_TAG = BTLEScanCallback.class.getSimpleName();
    public static final int DECODE_WINDOW_WIDTH = 2;

    @Override
    public void onScanResult(int callbackType, ScanResult result) {

        String remote = result.getDevice().getAddress();
        String local = BTLEConnectionManager.mBluetoothAdapter.getAddress();

        if (remote.equals(local)) {
            return;
        }

        if (result.getRssi() < Config.BLE_MIN_RSSI) {
            Log.d(LOG_TAG, "Omitting package due to RSSI value of " + result.getRssi());
        }

        try {
            byte[] receivedBytes = result.getScanRecord().getManufacturerSpecificData(Config.BLE_TAG);

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
                //WatchListenerService.receivedHandshakes.addHandshake(new HandshakeData(decodedMessage));
            } else {
                Log.e(LOG_TAG, "Error decoding the received message");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "No valid data using BLE_TAG");
        }
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
