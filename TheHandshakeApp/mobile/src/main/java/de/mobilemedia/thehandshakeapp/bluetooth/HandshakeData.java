package de.mobilemedia.thehandshakeapp.bluetooth;

import android.util.Log;

import java.io.Serializable;

public class HandshakeData implements Serializable, Comparable<HandshakeData> {
    private String longUrl;
    private String shortUrl;
    private static String prefix = "http://bit.ly/";
    private String msg;
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
        }

    }

    public HandshakeData(String shortUrl, String longUrl){
        this.msg = shortUrlToHash(shortUrl);
        Log.d("NEWHASH", this.msg);
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

    public String getMsg() { return this.msg; }

    public long getTimeStamp() {
        return this.timestamp;
    }
}
