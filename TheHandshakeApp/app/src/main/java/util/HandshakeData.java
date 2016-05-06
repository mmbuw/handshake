package util;

import java.io.Serializable;

/**
 * Created by projectsw on 06.05.16.
 */
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