package de.mobilemedia.thehandshakeapp.detection;

import java.util.LinkedList;

public abstract class HandshakeDetectedAction {

    public abstract void onHandshakeDetected(LinkedList<float[]> data,
                                             int startSample,
                                             int endSample);
}
