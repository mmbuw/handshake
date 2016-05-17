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
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;

public class SettingsFragment extends Fragment {

    MainActivity parentActivity;

    Button settingsApplyButton;
    Button clearButton;
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
        clearButton = (Button) view.findViewById(R.id.button_clear);
        urlTextView = (TextView) view.findViewById(R.id.setting_url_field);

        String currentUrl = parentActivity.getBleConnectionManager().getLongUrl();

        urlTextView.setText( currentUrl );

        settingsApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettingsApplyButtonClick();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                urlTextView.setText("");
            }
        });

        return view;
    }

    public void onSettingsApplyButtonClick() {

        String longUrl = urlTextView.getText().toString();
        Log.i("NEWURL", longUrl);

        try {
            String shortUrl = new Util.BitlyRequest()
                    .setMethod("v3/shorten")
                    .setContentType("&longUrl=")
                    .execute(longUrl).get();
            HandshakeData newHandshakeData = new HandshakeData(shortUrl, longUrl);
            parentActivity.getBleConnectionManager().setMyHandshake(newHandshakeData);
            /*TODO: do it better or add wait screen*/
            Toast.makeText(parentActivity, "Applied new Handshake URL:\n" + shortUrl, Toast.LENGTH_SHORT).show();
            Log.i("NEWSHORTURL", shortUrl);
        } catch (Exception e) {
            Toast.makeText(parentActivity, "Couldn't convert URL.", Toast.LENGTH_SHORT).show();
        }

    }

}
