package de.mobilemedia.thehandshakeapp.mobile_core;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;

public class HandshakeListFragment extends ListFragment {

    MainActivity parentActivity;

    private ArrayList<HandshakeData> handshakes;

    public HandshakeListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(parentActivity.getString(R.string.list_title));

        handshakes = ReceivedHandshakes.getInstance(getContext()).getHandshakes();

        setListAdapter(new HandshakeListAdapter(handshakes));
    }

    private class HandshakeListAdapter extends ArrayAdapter<HandshakeData> {
        public HandshakeListAdapter(ArrayList<HandshakeData> handshakes) {
            super(getActivity(), 0, handshakes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null){
                convertView = getActivity().getLayoutInflater()
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                HandshakeData hd = ((HandshakeListAdapter)getListAdapter()).getItem(position);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Sharing Handshake");
                i.putExtra(Intent.EXTRA_TEXT, hd.getShortUrl());
                startActivity(Intent.createChooser(i, "Share Handshake"));
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HandshakeData hd = ((HandshakeListAdapter)getListAdapter()).getItem(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String url = hd.getLongUrl();
        if (url == null ) url = hd.getShortUrl();

        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
