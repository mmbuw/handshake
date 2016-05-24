package de.mobilemedia.thehandshakeapp.bluetooth;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by neffle on 12.05.16.
 */
public class ReceivedHandshakes {
    private static ReceivedHandshakes receivedHandshakes;
    private Context appContext;
    private HashMap<String, HandshakeData> receivedHandshakesMap;
    private static BlockingQueue<HandshakeData> processingQueue = new LinkedBlockingQueue<HandshakeData>();

    private void startProcessing(){

//        TODO: check for internet connection

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        HandshakeData hd = processingQueue.take();
                        if (hd.getLongUrl() != null) continue;
                        String shortUrl = hd.getShortUrl();
                        Log.d("QUEUE", "Expanding URL: " + shortUrl);
                        String longUrl = new Util.BitlyRequest()
                                .setContentType("&shortUrl=")
                                .setMethod("/v3/expand")
                                .execute(shortUrl)
                                .get();
                        hd.setLongUrl(longUrl);
                    }
                    catch (Exception e){
                        Log.e("QUEUE", "Processing Queue interrupted.");
                    }
                }
            }
        }).start();

    }

    public static ReceivedHandshakes getInstance(Context c) {
        if (receivedHandshakes == null){
            receivedHandshakes = new ReceivedHandshakes(c);
        }
        return receivedHandshakes;
    }

    public HashMap<String, HandshakeData> getReceivedHandshakesMap() {
        return receivedHandshakesMap;
    }

    public void setReceivedHandshakesMap(HashMap<String, HandshakeData> receivedHandshakesMap) {
        this.receivedHandshakesMap = receivedHandshakesMap;
    }

    private ReceivedHandshakes(Context c) {
        this.appContext = c;
        this.receivedHandshakesMap = new HashMap<>();

        for (int i = 0; i < 2; i++) {
            String key = ""+i;
            HandshakeData data = new HandshakeData("1SRhxGT");
            receivedHandshakesMap.put(key, data);
            processingQueue.add(data);
        }

        this.startProcessing();

    }

    public ArrayList<HandshakeData> getHandshakes(){
        ArrayList<HandshakeData> list = new ArrayList<>();

        for (Map.Entry<String, HandshakeData> entry : receivedHandshakesMap.entrySet()) {
            list.add(entry.getValue());
        }

        Collections.sort(list);

        return list;
    }

    public void addHandshake(HandshakeData hd){
        String msg = hd.getShortUrl();
        if(!receivedHandshakesMap.containsKey(msg)){
            receivedHandshakesMap.put(msg, hd);
            processingQueue.add(hd);
            Log.d("MSG", "Added new message.");
        }
        else{
            //TODO: Maybe we can do this better.
            receivedHandshakesMap.get(msg).updateTimestamp(hd.getTimeStamp());
            Log.d("MSG", "Message already exists, updated timestamp.");
        }
    }
}
