package de.mobilemedia.thehandshakeapp.mobile_core;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.MessageData;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;

public class SettingsFragment extends Fragment {

    MainActivity parentActivity;

    Button settingsApplyButton;
    TextView urlTextView;

    public SettingsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(parentActivity.getString(R.string.fragment_setting_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsApplyButton = (Button) view.findViewById(R.id.settings_apply_button);
        urlTextView = (TextView) view.findViewById(R.id.setting_url_field);

        settingsApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettingsApplyButtonClick();
            }
        });

        return view;
    }

    public void onSettingsApplyButtonClick() {

        String newUrl = urlTextView.getText().toString();
        Log.i("NEWURL", newUrl);

        try {
            String shortUrl = new Util.BitlyShortenRequest().execute(newUrl).get();
            MessageData newMsgData = new MessageData(shortUrl, true);
            parentActivity.getBleConnectionManager().setMessageData(newMsgData);
            Log.i("NEWHASH", shortUrl);
        } catch (Exception e) {
            Toast.makeText(parentActivity, "Couldn't convert URL.", Toast.LENGTH_SHORT);
        }

    }

}
