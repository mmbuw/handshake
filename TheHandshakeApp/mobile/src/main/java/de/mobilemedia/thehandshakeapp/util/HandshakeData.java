package de.mobilemedia.thehandshakeapp.util;

import java.io.Serializable;

public class HandshakeData implements Serializable {
    String msg;
    Long timestamp;
    //HandshakeSignature signature;

    HandshakeData(String msg, Long ts){
        this.msg = msg;
        this.timestamp = ts;
    }

    public void updateTimestamp(long ts){
        this.timestamp = ts;
    }
}