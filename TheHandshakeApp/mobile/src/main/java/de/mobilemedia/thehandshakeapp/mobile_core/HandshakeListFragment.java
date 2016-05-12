package de.mobilemedia.thehandshakeapp.mobile_core;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        ArrayAdapter<HandshakeData> adapter =
            new ArrayAdapter<HandshakeData>(parentActivity,
                               R.layout.simple_list_item,
                               handshakes );

        setListAdapter(adapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                HandshakeData hd = (HandshakeData) (getListAdapter()).getItem(position);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Sharing Handshake");
                i.putExtra(Intent.EXTRA_TEXT, hd.getUrl());
                startActivity(Intent.createChooser(i, "Share Handshake"));
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HandshakeData hd = (HandshakeData) (getListAdapter()).getItem(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(hd.getUrl()));
        startActivity(intent);
    }
}
