package de.mobilemedia.thehandshakeapp.bluetooth;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss\t\t\td.M.yy");
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

                String urlString = address + method + token + shortUrl +format;

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


}
