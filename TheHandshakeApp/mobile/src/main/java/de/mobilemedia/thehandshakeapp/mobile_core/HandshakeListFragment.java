package de.mobilemedia.thehandshakeapp.mobile_core;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;
import de.mobilemedia.thehandshakeapp.mobile_ui.StableHandshakeListAdapter;

public class HandshakeListFragment extends ListFragment {

    public HandshakeListFragment() {}
    MainActivity parentActivity;
    StableHandshakeListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(parentActivity.getString(R.string.list_title));

        ArrayList<HandshakeData> handshakes = ReceivedHandshakes.getInstance(getContext()).getHandshakes();

        setListAdapter(new StableHandshakeListAdapter(parentActivity, handshakes));
        mAdapter = (StableHandshakeListAdapter) getListAdapter();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                HandshakeData hd = ((StableHandshakeListAdapter)getListAdapter()).getItem(position);
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
        HandshakeData hd = ((StableHandshakeListAdapter)getListAdapter()).getItem(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String url = hd.getLongUrl();
        if (url == null ) url = hd.getShortUrl();

        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
