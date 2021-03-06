package de.mobilemedia.thehandshakeapp.bluetooth;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.mobilemedia.thehandshakeapp.detection.WatchListenerService;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class ReceivedHandshakes {

    private HashMap<String, HandshakeData> receivedHandshakesMap;
    private static BlockingQueue<HandshakeData> processingQueue = new LinkedBlockingQueue<HandshakeData>();

    private boolean mInBackground;

    public ReceivedHandshakes(boolean inBackground) {
        this.receivedHandshakesMap = new HashMap<>();
        mInBackground = inBackground;
        this.startProcessing();
    }

    public void addFakeData() {
        HandshakeData fake1 = new HandshakeData("1XUCsew");
        HandshakeData fake2 = new HandshakeData("1SRhxGT");
        HandshakeData fake3 = new HandshakeData("1qwGEEr");

        addToProcessingQueue(fake1);
        addToProcessingQueue(fake2);
        addToProcessingQueue(fake3);

        addHandshake(fake1);
        addHandshake(fake2);
        addHandshake(fake3);
    }

    public HashMap<String, HandshakeData> getReceivedHandshakesMap() {
        return receivedHandshakesMap;
    }

    public void setReceivedHandshakesMap(HashMap<String, HandshakeData> receivedHandshakesMap) {
        this.receivedHandshakesMap = receivedHandshakesMap;

        if (mInBackground) {
            for (HandshakeData hd : receivedHandshakesMap.values()) {
                if (hd.getLongUrl() == null) {
                    addToProcessingQueue(hd);
                }
            }
        }
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
        String hash = hd.getHash();
        if(!receivedHandshakesMap.containsKey(hash)){
            receivedHandshakesMap.put(hash, hd);

            if (mInBackground) {
                addToProcessingQueue(hd);
            }
            //Log.d("MAP_ADD", "Added new Handshake with hash: "+hash);
        }
        else{
            receivedHandshakesMap.get(hash).updateTimestamp(hd.getTimeStamp());
            //Log.d("MAP_ADD", "Message already exists, updated timestamp.");
        }
    }

    public void removeHandshake(HandshakeData hd) {
        String hash = hd.getHash();
        if (receivedHandshakesMap.containsKey(hash)) receivedHandshakesMap.remove(hash);
        //Log.d("MAP_REMOVE", hash);
    }

    public void addToProcessingQueue(HandshakeData hd) {
        processingQueue.add(hd);
    }

    private void startProcessing(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        HandshakeData hd = processingQueue.take();
                        if (hd.getLongUrl() != null) continue;
                        String shortUrl = hd.getShortUrl();
                        //Log.d("QUEUE", "Expanding URL: " + shortUrl);
                        String longUrl = new Util.BitlyRequest()
                                .setContentType("&shortUrl=")
                                .setMethod("/v3/expand")
                                .execute(shortUrl)
                                .get();
                        hd.setLongUrl(longUrl);

                        if (mInBackground)
                            WatchListenerService.saveCurrentData();
                        else
                            MainActivity.saveCurrentData();
                    }
                    catch (Exception e){
                        Log.e("QUEUE", "Processing Queue interrupted.");
                    }
                }
            }
        }).start();

    }
}
