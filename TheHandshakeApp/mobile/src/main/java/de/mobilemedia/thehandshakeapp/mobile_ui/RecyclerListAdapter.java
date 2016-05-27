package de.mobilemedia.thehandshakeapp.mobile_ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.mobile_core.HandshakeListFragment;

/**
 * Inspiration:
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.wu0q1x1c9
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<HandshakeData> mHandshakes;
    private HandshakeListFragment.OnItemTouchListener onItemTouchListener;

    public RecyclerListAdapter(List<HandshakeData> handshakes
            ,HandshakeListFragment.OnItemTouchListener onItemTouchListener) {
        mHandshakes = handshakes;
        this.onItemTouchListener = onItemTouchListener;
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
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.setContent(mHandshakes.get(position), onItemTouchListener);
    }

    @Override
    public int getItemCount() {
        return mHandshakes.size();
    }

    @Override
    public void onItemDismiss(int position) {
        mHandshakes.remove(position);
        notifyItemRemoved(position);
    }
}
