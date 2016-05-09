package detection;

import com.example.projectsw.thehandshakeapp.MainActivity;

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
