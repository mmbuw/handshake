package de.mobilemedia.thehandshakeapp.mobile_ui;

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public final View itemView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public void setContent(HandshakeData hd){

        String longUrlStr = hd.getLongUrl();

        if(longUrlStr != null){
            TextView longUrl =
                    (TextView) itemView.findViewById(R.id.list_item_longurl);
            longUrl.setText(longUrlStr);
        }

        TextView shortUrl =
                (TextView) itemView.findViewById(R.id.list_item_shorturl);
        shortUrl.setText(hd.getShortUrl());

        TextView date =
                (TextView) itemView.findViewById(R.id.list_item_date);
        date.setText(hd.getDateString());
    }

}