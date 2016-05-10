package de.mobilemedia.thehandshakeapp.mobile_core;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.detection.FeatureExtractor;
import de.mobilemedia.thehandshakeapp.detection.HandshakeDetectedBluetoothAction;
import de.mobilemedia.thehandshakeapp.bluetooth.BleConnectionManager;
import de.mobilemedia.thehandshakeapp.bluetooth.HandshakeData;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private NavigationView navigationView;
    private MainFragment mainFragment;

    private AccelerationDataReceiver serviceReceiver;
    private FeatureExtractor featureExtractor;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BleConnectionManager bleConnectionManager;
    public static HashMap<String, HandshakeData> receivedHandshakes = new HashMap<>();

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Create initial fragment display */
        mainFragment = new MainFragment();
        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        /* Create toolbar and navigation view */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Request permissions */
        verifyStoragePermissions();
        verifyLocationPermissions();
        verifyPhoneStatePermissions();

        /* Create Bluetooth LE connection manager */
        bleConnectionManager = new BleConnectionManager(getApplicationContext());

        /* Create Google API client */
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /* Register broadcast receiver */
        serviceReceiver = new AccelerationDataReceiver();
        IntentFilter intentSFilter = new IntentFilter("accelerationAction");
        registerReceiver(serviceReceiver, intentSFilter);

        /* Init feature extractor */
        featureExtractor = new FeatureExtractor(3,    // number of data columns
                                                1,    // index of major axis column
                                                1,    // samples for peak detection
                                                5.0f, // peak amplitude threshold
                                                15,   // peak repeat threshold
                                                0,    // moving average window width
                                                30,   // alternation time max diff
                                                5,    // alternation count detection threshold
                                                new HandshakeDetectedBluetoothAction(mainFragment));
                                                //new HandshakeDetectedToastAction(getApplicationContext()));
    }

    /* Requests the necessary storage permissions from the operating system */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /* Requests the necessary location permissions from the operating system */
    public void verifyLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

    /* Requests the necessary phone state permissions from the operating system */
    public void verifyPhoneStatePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    0);
        }
    }

    public BleConnectionManager getBleConnectionManager() {
        return bleConnectionManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_handshake) {
            MainFragment mainFragment = new MainFragment();
            FragmentTransaction fragmentTransaction
                    = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_list) {

        } else if (id == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentTransaction fragmentTransaction
                    = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    /* Internal receiver class to get data from background service */
    public class AccelerationDataReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            float[] receivedValues = notificationData.getFloatArray("acceleration");
            mainFragment.processReceivedValues(receivedValues);
            featureExtractor.processDataRecord(receivedValues);
        }

    }

}