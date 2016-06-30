package de.mobilemedia.thehandshakeapp.mobile_ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.mobile_core.HandshakeListFragment;
import de.mobilemedia.thehandshakeapp.mobile_core.MainActivity;

/**
 * Inspiration:
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.wu0q1x1c9
 * https://developer.android.com/training/material/lists-cards.html
 * http://stackoverflow.com/questions/4184382/how-to-implement-both-ontouch-and-also-onfling-in-a-same-listview
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<HandshakeData> mHandshakes;
    private HandshakeListFragment.OnItemTouchListener onItemTouchListener;
    private HandshakeListFragment handshakeListFragment;

    public RecyclerListAdapter(HandshakeListFragment handshakeListFragment, List<HandshakeData> handshakes
            , HandshakeListFragment.OnItemTouchListener onItemTouchListener) {
        mHandshakes = handshakes;
        this.onItemTouchListener = onItemTouchListener;
        this.handshakeListFragment = handshakeListFragment;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {

        HandshakeData hd = mHandshakes.get(position);

        String longUrlStr = hd.getLongUrl();

        if(longUrlStr != null) holder.longUrl.setText(longUrlStr);

        holder.shortUrl.setText(hd.getShortUrl());

        holder.date.setText(hd.getDateString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemTouchListener.onHandshakeTap(view, position);
            }
        });
        
    }

    @Override
    public int getItemCount() {
        return mHandshakes.size();
    }

    @Override
    public void onItemDismiss(int position) {
        this.handshakeListFragment.removeHandshake(position);
        notifyItemRemoved(position);
        MainActivity.saveCurrentData();
    }
}
