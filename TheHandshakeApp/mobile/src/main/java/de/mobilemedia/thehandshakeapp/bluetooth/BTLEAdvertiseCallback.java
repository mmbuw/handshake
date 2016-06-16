package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

public class BTLEAdvertiseCallback extends AdvertiseCallback {

    public static final String LOG_TAG = BTLEAdvertiseCallback.class.getSimpleName();

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        super.onStartSuccess(settingsInEffect);
    }

    @Override
    public void onStartFailure(int errorCode) {
        Log.e(LOG_TAG, "Advertising onStartFailure: " + errorCode);
        super.onStartFailure(errorCode);
    }

}
