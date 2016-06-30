package de.mobilemedia.thehandshakeapp.bluetooth;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceivedHandshakes {

    private HashMap<String, HandshakeData> receivedHandshakesMap;
    private static BlockingQueue<HandshakeData> processingQueue = new LinkedBlockingQueue<HandshakeData>();

    public ReceivedHandshakes() {
        this.receivedHandshakesMap = new HashMap<>();
        addHandshake(new HandshakeData("1XUCsew"));
        addHandshake(new HandshakeData("1SRhxGT"));
        addHandshake(new HandshakeData("1qwGEEr"));
        this.startProcessing();

    }

    public HashMap<String, HandshakeData> getReceivedHandshakesMap() {
        return receivedHandshakesMap;
    }

    public void setReceivedHandshakesMap(HashMap<String, HandshakeData> receivedHandshakesMap) {
        this.receivedHandshakesMap = receivedHandshakesMap;
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
            processingQueue.add(hd);
            Log.d("MAP_ADD", "Added new Handshake with hash: "+hash);
        }
        else{
            receivedHandshakesMap.get(hash).updateTimestamp(hd.getTimeStamp());
            Log.d("MAP_ADD", "Message already exists, updated timestamp.");
        }
    }

    public void removeHandshake(HandshakeData hd) {
        String hash = hd.getHash();
        if (receivedHandshakesMap.containsKey(hash)) receivedHandshakesMap.remove(hash);
        Log.d("MAP_REMOVE", hash);
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
}
