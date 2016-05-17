package de.mobilemedia.thehandshakeapp.bluetooth;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by neffle on 12.05.16.
 */
public class ReceivedHandshakes {
    private static ReceivedHandshakes receivedHandshakes;
    private Context appContext;
    private HashMap<String, HandshakeData> receivedHandshakesMap;

    public static ReceivedHandshakes getInstance(Context c) {
        if (receivedHandshakes == null){
            receivedHandshakes = new ReceivedHandshakes(c);
        }
        return receivedHandshakes;
    }

    private ReceivedHandshakes(Context c) {
        this.appContext = c;
        this.receivedHandshakesMap = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            String key = ""+i;
            HandshakeData data = new HandshakeData("1rM17oR");
            receivedHandshakesMap.put(key, data);
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
        String msg = hd.msg;
        if(!receivedHandshakesMap.containsKey(msg)){
            receivedHandshakesMap.put(msg, hd);
            Log.d("MSG", "Added new message.");
        }
        else{
            //TODO: Maybe we can do this better.
            receivedHandshakesMap.get(msg).updateTimestamp(hd.timestamp);
            Log.d("MSG", "Message already exists, updated timestamp.");
        }
    }
}
