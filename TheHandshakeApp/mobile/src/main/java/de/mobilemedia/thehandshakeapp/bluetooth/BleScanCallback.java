package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.util.SparseArray;

import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class BleScanCallback extends ScanCallback {
    private byte[] data;
    @Override public void onScanResult(int callbackType, ScanResult result) {
        SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();
        long timestampNanos = result.getTimestampNanos();
        String msg = "";
        try {
            Log.d("MAN_DATA", manufacturerSpecificData.toString());
            msg = new String(manufacturerSpecificData.get(BleConnectionManager.BLE_TAG));
            Log.d("MSG", msg);
            MainActivity.receivedHandshakes.addHandshake(new HandshakeData(msg));
        }
        catch (Exception e){
            Log.e("MAN_DATA",e.toString());
        }
    }
}
