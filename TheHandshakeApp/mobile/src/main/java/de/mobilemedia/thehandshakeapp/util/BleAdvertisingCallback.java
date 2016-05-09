package de.mobilemedia.thehandshakeapp.util;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

/**
 * Created by projectsw on 02.05.16.
 */
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
