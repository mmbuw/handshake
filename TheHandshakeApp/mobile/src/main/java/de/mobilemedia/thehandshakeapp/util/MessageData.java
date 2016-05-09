package de.mobilemedia.thehandshakeapp.util;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by projectsw on 06.05.16.
 */
public class MessageData implements Serializable{

    public URL url;
    public String hash;
    public static String prefix = "http://bit.ly/";

    public MessageData(String str, boolean isUrl) throws Exception {
        if (isUrl){
            this.url = new URL(str);
            generateHash();
        }
        else{
            this.hash = str;
            generateUrl();
        }

    }

    private void generateHash() throws Exception {
        //TODO
        this.hash = "1SRhxGT";
    }

    private void generateUrl() throws Exception {
        //TODO
        this.url = new URL("http://www.chefkoch.de/rezepte/569881155648994/Schrats-Baerlauchpesto.html");
    }

}
