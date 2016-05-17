package de.mobilemedia.thehandshakeapp.bluetooth;

import java.io.Serializable;

public class HandshakeData implements Serializable, Comparable<HandshakeData> {
    String msg;
    String longUrl;
    static String shortUrlPrefix = "http://bit.ly/";
    Long timestamp;
    //HandshakeSignature signature;

    public HandshakeData(String msg){
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
        return (int) (hd.timestamp - this.timestamp);
    }

    public String getShortUrl(){
        return shortUrlPrefix + this.msg;
    }

    public String getDateString() {
        return Util.nanoTimeToDateString(timestamp);
    }
}
