package de.mobilemedia.thehandshakeapp.bluetooth;

import java.io.Serializable;

public class HandshakeData implements Serializable, Comparable<HandshakeData> {
    String msg;
    Long timestamp;
    //HandshakeSignature signature;

    HandshakeData(String msg){
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }

    public void updateTimestamp(long ts){
        this.timestamp = ts;
    }

    @Override
    public String toString() {
        return msg+"\n"+Util.nanoTimeToDateString(timestamp);
    }

    @Override
    public int compareTo(HandshakeData hd) {
        return (int) (this.timestamp - hd.timestamp);
    }

    public String getUrl(){
        return "http://bit.ly/"+this.msg;
    }
}
