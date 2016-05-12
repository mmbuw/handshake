package de.mobilemedia.thehandshakeapp.mobile_core;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

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

}
