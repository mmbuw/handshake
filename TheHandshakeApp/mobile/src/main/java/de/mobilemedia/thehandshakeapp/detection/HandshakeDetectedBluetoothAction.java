package de.mobilemedia.thehandshakeapp.detection;

import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class HandshakeDetectedBluetoothAction extends HandshakeDetectedAction {

    MainActivity mActivityInstance;

    public HandshakeDetectedBluetoothAction(MainActivity activityInstance) {
        mActivityInstance = activityInstance;
    }

    @Override
    public void onHandshakeDetected() {
        mActivityInstance.onScanButtonClick();
    }
}
