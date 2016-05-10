package de.mobilemedia.thehandshakeapp.detection;

import android.content.Context;
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

        Context context = mFragmentInstance.getContext();

        if (context != null) {
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
