package de.mobilemedia.thehandshakeapp.detection;

import android.widget.Toast;

import java.util.Date;

import de.mobilemedia.thehandshakeapp.mobile_core.MainFragment;

public class HandshakeDetectedBluetoothAction extends HandshakeDetectedAction {

    MainFragment mFragmentInstance;

    public HandshakeDetectedBluetoothAction(MainFragment fragmentInstance) {
        mFragmentInstance = fragmentInstance;
    }

    @Override
    public void onHandshakeDetected() {
        mFragmentInstance.onScanButtonClick();
        String text = "Handshake detected";
        text += "\n" + new Date().toString();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mFragmentInstance.getContext(), text, duration);
        toast.show();
    }
}
