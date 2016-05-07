package mobile_core;

import android.content.Context;
import android.widget.Toast;

import java.util.Date;

public class HandshakeDetectedToastAction extends HandshakeDetectedAction {

    private Context mContext;

    public HandshakeDetectedToastAction(Context context) {
        mContext = context;
    }

    @Override
    public void onHandshakeDetected() {

        String text = "Handshake detected";
        text += "\n" + new Date().toString();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();

    }
}
