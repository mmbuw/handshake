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

        String currentUrl = parentActivity.getBleConnectionManager().getCurrentUrl();

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

        String newUrl = urlTextView.getText().toString();
        Log.i("NEWURL", newUrl);

        try {
            String shortUrl = new Util.BitlyShortenRequest().execute(newUrl).get();
            HandshakeData newhandshakeData = new HandshakeData(shortUrl);
            parentActivity.getBleConnectionManager().setMyHandshake(newhandshakeData);
            /*TODO: do it better or add wait screen*/
            Toast.makeText(parentActivity, "Applied new Handshake URL:\n" + shortUrl, Toast.LENGTH_SHORT).show();
            Log.i("NEWHASH", shortUrl);
        } catch (Exception e) {
            Toast.makeText(parentActivity, "Couldn't convert URL.", Toast.LENGTH_SHORT).show();
        }

    }

}
