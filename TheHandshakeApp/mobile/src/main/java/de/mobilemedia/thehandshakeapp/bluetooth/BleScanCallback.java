package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.util.SparseArray;

import de.mobilemedia.thehandshakeapp.mobile_core.Config;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class BleScanCallback extends ScanCallback {

    private byte[] data;
    private int decodeWindowWidth = 1;

    @Override public void onScanResult(int callbackType, ScanResult result) {
        SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();

        Log.d("BLE", String.format("received manufacturer specific data %s"
                , manufacturerSpecificData.toString()));

        try {

            byte[] receivedBytes = manufacturerSpecificData.get(Config.BLE_TAG);
            int unixTimestamp = Util.getCurrentUnixTimestamp();
            String decodedMessage = null;

            System.out.println("IN DATA IS: ");
            Util.printByteArray(receivedBytes);

            for (int timePos = unixTimestamp-decodeWindowWidth;
                 timePos <= unixTimestamp+decodeWindowWidth;
                 ++timePos) {

                byte[] decoded = Util.endecrypt(receivedBytes, timePos);
                String msg = new String(decoded);
                System.out.println("Testing key " + timePos + " with result " + msg);

                if (msg.startsWith("!") && msg.length() == 8) {
                    decodedMessage = msg.substring(1);
                    break;
                }

            }

            if (decodedMessage != null) {
                Log.d("BLE", String.format("data contained the message %s", decodedMessage));
                MainActivity.receivedHandshakes.addHandshake(new HandshakeData(decodedMessage));
            } else {
                Log.e("BLE", "Error decoding the received message.");
            }
        }
        catch (Exception e){
            Log.d("BLE", "couldn't find our BLE_TAG, probably some other data.");
        }
    }
}
