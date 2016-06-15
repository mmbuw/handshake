package de.mobilemedia.thehandshakeapp.bluetooth;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Util {
        public static String doApiRequest(URL url) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

    public static String nanoTimeToDateString(long nanotime){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss\t-\td.MM.yy");
        return sdf.format(new Date(nanotime));
    }

    public static String apiRequest(String urlString) throws IOException {

        String content = "";

        URL url = new URL(urlString);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");

        urlConnection.connect();

        content = readAsText(urlConnection.getInputStream());

        return content;
    }

    public static class BitlyRequest extends AsyncTask<String, Void, String> {

        String method;
        String contentType;
        String format = "&format=txt";

        public BitlyRequest setMethod(String method) {
            this.method = method;
            return this;
        }

        public BitlyRequest setContentType(String content) {
            this.contentType = content;
            return this;
        }

        public BitlyRequest setFormat(String format) {
            this.format = format;
            return this;
        }

        @Override
        protected String doInBackground(String... urls) {

            String response = "";

            try{
                String encodedUrl = URLEncoder.encode(urls[0], "UTF-8");
                String address =  "https://api-ssl.bitly.com/";
                //TODO: Maybe we should put this somewhere else :)
                String token = "?access_token=d9bf1e2bdc0a4d6f585829ec9bf0d128b6be586c";
                String content = contentType + encodedUrl;
                String urlString = address + method + token + content + format;

                response = apiRequest(urlString);

            }
            catch (Exception e){
                Log.e("APIREQUEST", urls[0] + " " + method + " request failed.");
                Log.d("APIREQUEST", e.toString());
                e.printStackTrace();
            }

            return response;
        }

    }

    public static class BitlyShortenRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String shortenedLink = "";

            try{
                String encodedUrlToShorten = URLEncoder.encode(urls[0], "UTF-8");

                String address =  "https://api-ssl.bitly.com/";
                String method = "v3/shorten";
                //TODO: Maybe we should put this somewhere else :)
                String token = "?access_token=d9bf1e2bdc0a4d6f585829ec9bf0d128b6be586c";
                String shortUrl = "&longUrl="+encodedUrlToShorten;
                String format = "&format=txt";

                String urlString = address + method + token + shortUrl + format;

                shortenedLink = apiRequest(urlString);

            }
            catch (Exception e){
                Log.e("APIREQUEST", urls[0] + " shorten request failed.");
                Log.d("APIREQUEST", e.toString());
                e.printStackTrace();
            }

            return shortenedLink;
        }

    }

    /**
     *
     * Inspiration: http://www.mkyong.com/java/how-to-convert-inputstream-to-string-in-java/
     * @param is
     * @return String
     */
    public static String readAsText(InputStream is)  {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void saveMapToFile(Map map, File file){
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
            Log.d("SAVE", "saved map to file "+file.getAbsolutePath());
        } catch(IOException i) {
            Log.d("SAVE", "couldn't save map to file "+file.getAbsolutePath());
        }
    }

    public static Map loadMapFromFile(File file){
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Map map = (Map) in.readObject();

            in.close();
            fileIn.close();
            Log.d("LOAD", "loaded map from file " + file.getAbsolutePath());

            return map;
        } catch(IOException i) {
            Log.d("LOAD", "couldn't load map from file "+file.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            Log.d("LOAD", "couldn't load map, class not found");
        }
        return null;
    }

    public static int getCurrentUnixTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    /* ENCRYPTION AND DECRYPTION HELPER FUNCTIONS */

    public static byte[] endecrypt(byte[] inputText, int key) {

        byte[] keyBytes = ByteBuffer.allocate(4).putInt(key).array();
        byte[] stretchedKeyBytes = stretchKeySHA512(keyBytes, inputText.length);
        byte[] encryption = xorBytes(inputText, stretchedKeyBytes);

        return encryption;
    }

    public static byte[] stretchKeySHA512(byte[] keyByteArray, int outputLength) {

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
            return null;
        }

        byte[] sha512 = md.digest(keyByteArray);
        byte[] trimmedOutput = new byte[outputLength];

        if (outputLength > sha512.length) {
            System.err.println("Error: message too long");
            return null;
        }

        for (int pos = 0; pos < outputLength; ++pos) {
            trimmedOutput[pos] = sha512[pos];
        }

        return trimmedOutput;

    }

    public static byte[] xorBytes(byte[] lhs, byte[] rhs) {

        if (lhs.length != rhs.length) {
            System.err.println("Error (xor): Array sizes do not match.");
            return null;
        }

        byte[] output = new byte[lhs.length];

        for (int pos = 0; pos < lhs.length; ++pos) {
            output[pos] = (byte) (lhs[pos] ^ rhs[pos]);
        }

        return output;

    }

    public static void printByteArray(byte[] toPrint) {
        String printString = "";

        for (byte b : toPrint) {
            printString += String.format("%02X ", b);
        }

        System.out.println(printString);
    }

}
