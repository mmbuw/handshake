package com.example.projectsw.hellosmartwatch;

import android.content.Context;
import android.widget.Toast;

public class HandshakeDetectedToastAction extends HandshakeDetectedAction {

    private Context mContext;

    public HandshakeDetectedToastAction(Context context) {
        mContext = context;
    }

    @Override
    public void onHandshakeDetected() {

        CharSequence text = "Handshake detected";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();

    }
}
