package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.util.SparseArray;

import de.mobilemedia.thehandshakeapp.mobile_core.Config;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class BleScanCallback extends ScanCallback {
    private byte[] data;
    @Override public void onScanResult(int callbackType, ScanResult result) {
        SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();

        Log.d("BLE", String.format("received manufacturer specific data %s"
                , manufacturerSpecificData.toString()));

        try {
            String msg = new String(manufacturerSpecificData.get(Config.BLE_TAG));

            Log.d("BLE", String.format("data contained the message %s", msg));

            MainActivity.receivedHandshakes.addHandshake(new HandshakeData(msg));
        }
        catch (Exception e){
            Log.d("BLE", "couldn't find our BLE_TAG, probably some other data.");
        }
    }
}
