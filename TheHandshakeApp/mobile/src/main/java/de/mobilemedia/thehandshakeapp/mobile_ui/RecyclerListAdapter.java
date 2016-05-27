package de.mobilemedia.thehandshakeapp.mobile_ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;

public class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private final List<HandshakeData> mHandshakes = new ArrayList<>();

    public RecyclerListAdapter(List<HandshakeData> handshakes) {
        mHandshakes.addAll(handshakes);
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
        holder.setContent(mHandshakes.get(position));
    }

    @Override
    public int getItemCount() {
        return mHandshakes.size();
    }

}
