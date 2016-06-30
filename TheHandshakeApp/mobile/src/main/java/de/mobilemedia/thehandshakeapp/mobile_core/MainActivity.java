package de.mobilemedia.thehandshakeapp.mobile_core;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;
import de.mobilemedia.thehandshakeapp.detection.InternalAccelerationListenerService;
import de.mobilemedia.thehandshakeapp.detection.WatchListenerService;

import static de.mobilemedia.thehandshakeapp.bluetooth.Util.*;
import static de.mobilemedia.thehandshakeapp.bluetooth.Util.saveMapToFile;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static String ANDROID_ID;
    public static File FILE_STORAGE_PATH;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private MainFragment mainFragment;

    private BroadcastReceiver mServiceNotificationReceiver;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    public static ReceivedHandshakes receivedHandshakes;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ANDROID_ID = Settings.Secure.getString(getContentResolver(),
                     Settings.Secure.ANDROID_ID);
        FILE_STORAGE_PATH = this.getFilesDir();

        /* Request permissions */
        verifyPermissions();

        /* Create initial fragment display */
        mainFragment = new MainFragment();
        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        /* Create toolbar and navigation view */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        receivedHandshakes = new ReceivedHandshakes();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Create Google API client */
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /* Notification receiver */
        mServiceNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getStringExtra(WatchListenerService.ACTION_EXTRA_TAG);

                if (action.equals(WatchListenerService.DATA_NOTIFICATION_ACTION))
                    mainFragment.updateUiOnValuesReceived();
                else if (action.equals(WatchListenerService.HANDSHAKE_NOTIFICATION_ACTION))
                    mainFragment.updateUiOnHandshake();
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mServiceNotificationReceiver),
                new IntentFilter(WatchListenerService.SERVICE_TO_ACTIVITY_NAME)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceNotificationReceiver);
        super.onStop();
    }

    public static void loadPrevData(){
        File handshakesFile = new File(FILE_STORAGE_PATH, Config.HANDSHAKE_FILE_NAME);
        Map savedHandshakes = loadMapFromFile(handshakesFile);
        if (savedHandshakes != null) {
            receivedHandshakes.setReceivedHandshakesMap((HashMap<String, HandshakeData>) savedHandshakes);
            Log.d(LOG_TAG, "Handshakes loaded");
        }
    }

    public static void saveCurrentData(){
        HashMap<String, HandshakeData> handshakesMap = receivedHandshakes.getReceivedHandshakesMap();
        File handshakesFile = new File(FILE_STORAGE_PATH, Config.HANDSHAKE_FILE_NAME);
        saveMapToFile(handshakesMap, handshakesFile);
        Log.d(LOG_TAG, "Handshakes saved");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveCurrentData();
        super.onSaveInstanceState(outState);
    }

    /* Requests the necessary permissions from the operating system */
    public void verifyPermissions() {

        boolean permissionRequestNeeded = false;

        for (String permission : PERMISSIONS_STORAGE) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionRequestNeeded = true;
                break;
            }
        }

        if (permissionRequestNeeded) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(getApplicationContext(), InternalAccelerationListenerService.class );
        stopService(stopIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_handshake) {
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        } else if (id == R.id.nav_list) {
            loadPrevData();
            HandshakeListFragment listFragment = new HandshakeListFragment();
            fragmentTransaction.replace(R.id.fragment_container, listFragment);
        } else if (id == R.id.nav_settings) {
            PrefsFragement prefsFragement = new PrefsFragement();
            fragmentTransaction.replace(R.id.fragment_container, prefsFragement);
        }

        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
