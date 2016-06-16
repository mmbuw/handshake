package de.mobilemedia.thehandshakeapp.bluetooth;

import android.util.Log;

import java.io.Serializable;

public class HandshakeData implements Serializable, Comparable<HandshakeData> {

    public static final String MESSAGE_START_DELIMETER = "!!";

    private String longUrl;
    private String shortUrl;
    private static String prefix = "http://bit.ly/";
    private String hash;
    private Long timestamp;
    //HandshakeSignature signature;

    public HandshakeData(String urlOrHash){
        this.timestamp = System.currentTimeMillis();
        if (urlOrHash.startsWith("http")){
            if(urlOrHash.contains("bit.ly")){
                this.shortUrl = urlOrHash;
            }
            else{
                this.longUrl = urlOrHash;
            }
        }
        else{
            this.shortUrl = prefix + urlOrHash;
            this.hash = urlOrHash;
        }

    }

    public HandshakeData(String shortUrl, String longUrl){
        this.hash = shortUrlToHash(shortUrl);
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
    }

    public void updateTimestamp(long ts){
        this.timestamp = ts;
    }

    @Override
    public String toString() {
        return shortUrl+"\n"+Util.nanoTimeToDateString(timestamp);
    }

    @Override
    public int compareTo(HandshakeData hd) {
        return (int) (hd.timestamp - this.timestamp);
    }

    public String getShortUrl(){ return this.shortUrl; }

    public String getLongUrl(){
        return this.longUrl;
    }

    public String getDateString() {
        return Util.nanoTimeToDateString(timestamp);
    }

    private String shortUrlToHash(String shortUrl){
        String[] tmp = shortUrl.split("/");
        return tmp[tmp.length-1];
    }

    public String getHash() { return this.hash; }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getMessageToTransceive() {
        return MESSAGE_START_DELIMETER + getHash();
    }
}
