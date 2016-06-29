package de.mobilemedia.thehandshakeapp.mobile_core;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;
import de.mobilemedia.thehandshakeapp.detection.FileOutputWriter;

public class PrefsFragement extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    MainActivity mParentActivity;
    SharedPreferences mSharedPreferences;

    String mUrlPrefName;
    String mUrlShortPrefName;
    String mSuffixPrefName;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_preferences);
        mParentActivity = (MainActivity) getActivity();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUrlPrefName = getString(R.string.url_pref_id);
        mUrlShortPrefName = getString(R.string.url_short_pref_id);
        mSuffixPrefName = getString(R.string.suffix_pref_id);

        Preference urlPref = findPreference(mUrlPrefName);
        urlPref.setSummary(beautifyPreferenceString(mSharedPreferences.getString(mUrlPrefName, "")));
        Preference urlShortPref = findPreference(mUrlShortPrefName);
        urlShortPref.setSummary(beautifyPreferenceString(mSharedPreferences.getString(mUrlShortPrefName, "")));
        urlShortPref.setEnabled(false);
        Preference suffixPref = findPreference(mSuffixPrefName);
        suffixPref.setSummary(beautifyPreferenceString(mSharedPreferences.getString(mSuffixPrefName, "")));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = findPreference(key);
        String newPrefValue = sharedPreferences.getString(key, "");
        preference.setSummary(beautifyPreferenceString(newPrefValue));

        if (key.equals(mUrlPrefName)) {
            onURLPreferenceChange(newPrefValue);
        } else if (key.equals(mSuffixPrefName)) {
            onSuffixPreferenceChange(newPrefValue);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void onURLPreferenceChange(String newUrl) {

        try {
            String shortUrl = new Util.BitlyRequest()
                    .setMethod("v3/shorten")
                    .setContentType("&longUrl=")
                    .execute(newUrl).get();
            HandshakeData newHandshakeData = new HandshakeData(shortUrl, newUrl);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mUrlShortPrefName, shortUrl);
            editor.commit();

            mParentActivity.getBleConnectionManager().setMyHandshake(newHandshakeData);
            /*TODO: do it better or add wait screen*/
            Toast.makeText(mParentActivity, "Applied new Handshake URL:\n" + shortUrl, Toast.LENGTH_SHORT).show();
            Log.i("NEWSHORTURL", shortUrl);

        } catch (Exception e) {
            Toast.makeText(mParentActivity, "Couldn't convert URL.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSuffixPreferenceChange(String newSuffix) {
        FileOutputWriter.filePostfix = newSuffix;
        Toast.makeText(mParentActivity, "Applied new file suffix:\n" + beautifyPreferenceString(newSuffix),
                       Toast.LENGTH_SHORT).show();
    }

    private String beautifyPreferenceString(String input) {
        if (input.equals("")) {
            return "<empty>";
        }

        return input;
    }

}
