package de.mobilemedia.thehandshakeapp.mobile_core;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;
import de.mobilemedia.thehandshakeapp.mobile_ui.RecyclerListAdapter;
import de.mobilemedia.thehandshakeapp.mobile_ui.SimpleItemTouchHelperCallback;

public class HandshakeListFragment extends Fragment {

    public HandshakeListFragment() {}
    MainActivity parentActivity;
    ReceivedHandshakes mReceivedHandshakes;
    List<HandshakeData> mHandshakes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(parentActivity.getString(R.string.list_title));
        mReceivedHandshakes = MainActivity.receivedHandshakes;
        mHandshakes = mReceivedHandshakes.getHandshakes();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        OnItemTouchListener itemTouchListener = new OnItemTouchListener() {
            @Override
            public void onHandshakeTap(View view, int position) {
                HandshakeData hd = mHandshakes.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = hd.getLongUrl();
                if (url == null ) url = hd.getShortUrl();
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        };

        for (HandshakeData hd : mHandshakes) {
            if (hd.getLongUrl() == null)
            {
                mReceivedHandshakes.addToProcessingQueue(hd);
            }
        }

        RecyclerListAdapter adapter = new RecyclerListAdapter(this, mHandshakes, itemTouchListener);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

    }

    public interface OnItemTouchListener {
        void onHandshakeTap(View view, int position);
    }

    public void removeHandshake(int position){
        HandshakeData remove = mHandshakes.remove(position);
        mReceivedHandshakes.removeHandshake(remove);
    }

}
