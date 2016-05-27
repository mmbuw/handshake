package de.mobilemedia.thehandshakeapp.mobile_ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

public class StableHandshakeListAdapter extends ArrayAdapter<HandshakeData> {

    HashMap<HandshakeData, Integer> mIdMap = new HashMap<>();
    Context mContext;

    public StableHandshakeListAdapter(Context context, ArrayList<HandshakeData> handshakes) {
        super(context, 0, handshakes);
        mContext = context;
        for (int i = 0; i < handshakes.size(); ++i) {
            mIdMap.put(handshakes.get(i), i);
        }
    }

    @Override
    public boolean hasStableIds() {
        return super.hasStableIds();
    }

    @Override
    public long getItemId(int position) {
        HandshakeData item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public void remove(HandshakeData handshakeData) {
        mIdMap.remove(handshakeData);
        ((MainActivity) mContext).removeHandshake(handshakeData);
        super.remove(handshakeData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){
            convertView = ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.list_item, null);
        }

        HandshakeData hd = getItem(position);

        String longUrlStr = hd.getLongUrl();

        if(longUrlStr != null){
            TextView longUrl =
                    (TextView) convertView.findViewById(R.id.list_item_longurl);
            longUrl.setText(longUrlStr);
        }

        TextView shortUrl =
                (TextView) convertView.findViewById(R.id.list_item_shorturl);
        shortUrl.setText(hd.getShortUrl());

        TextView date =
                (TextView) convertView.findViewById(R.id.list_item_date);
        date.setText(hd.getDateString());

        return convertView;
    }


}
