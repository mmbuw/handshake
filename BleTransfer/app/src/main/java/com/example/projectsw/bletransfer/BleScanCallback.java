package com.example.projectsw.bletransfer;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by projectsw on 02.05.16.
 */
public class BleScanCallback extends ScanCallback {
    private byte[] data;
    @Override public void onScanResult(int callbackType, ScanResult result) {
        SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();
        long timestampNanos = result.getTimestampNanos();
        String msg = "";
        try {
            Log.d("MAN_DATA", manufacturerSpecificData.toString());
            msg = new String(manufacturerSpecificData.get(MainActivity.BLE_TAG));
            Log.d("MSG", msg);
            if(!MainActivity.receivedHandshakes.containsKey(msg)){
                MainActivity.receivedHandshakes.put(msg, new HandshakeData(msg, timestampNanos));
                Log.d("MSG", "Added new message.");
            }
            else{
                //TODO: Maybe we can do this better.
                MainActivity.receivedHandshakes.get(msg).updateTimestamp(timestampNanos);
                Log.d("MSG", "Message already exists, updated timestamp.");
            }

        }
        catch (Exception e){
            Log.e("MAN_DATA",e.toString());
        }
    }
}
