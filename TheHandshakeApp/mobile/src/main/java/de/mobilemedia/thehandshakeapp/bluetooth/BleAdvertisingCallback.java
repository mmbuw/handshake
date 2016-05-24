package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

public class BleAdvertisingCallback extends AdvertiseCallback {
    @Override
    public void onStartFailure(int errorCode) {
        Log.d("BT", String.format("advertising failed: %d",errorCode));
    }
    @Override
    public void onStartSuccess(AdvertiseSettings settings) {
        Log.d("BT", "advertising started");
    }
}
