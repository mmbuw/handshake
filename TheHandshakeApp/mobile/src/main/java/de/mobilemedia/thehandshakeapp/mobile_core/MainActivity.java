package de.mobilemedia.thehandshakeapp.mobile_core;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.BleConnectionManager;
import de.mobilemedia.thehandshakeapp.bluetooth.ReceivedHandshakes;
import de.mobilemedia.thehandshakeapp.detection.FeatureExtractor;
import de.mobilemedia.thehandshakeapp.detection.HandshakeDetectedBluetoothAction;
import de.mobilemedia.thehandshakeapp.detection.InternalAccelerationListenerService;
import de.mobilemedia.thehandshakeapp.detection.MRDFeatureExtractor;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private NavigationView navigationView;
    private MainFragment mainFragment;

    private AccelerationDataReceiver serviceReceiver;
    private MRDFeatureExtractor featureExtractor;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private BleConnectionManager bleConnectionManager;
    public static ReceivedHandshakes receivedHandshakes;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        receivedHandshakes
                = ReceivedHandshakes.getInstance(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        /* Create Google API client */
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /* Register broadcast receiver */
        serviceReceiver = new AccelerationDataReceiver();
        IntentFilter intentSFilter = new IntentFilter("accelerationAction");
        registerReceiver(serviceReceiver, intentSFilter);


        /* Init feature extractor */
        File trainingModelPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File trainingModelFile = new File(trainingModelPath, "trained-j48.model");

        featureExtractor = new MRDFeatureExtractor(3,   // number of data columns
                                                   1,   // samples for peak detection
                                                   150, // feature window width
                                                   trainingModelFile.toString(),
                                                   new HandshakeDetectedBluetoothAction(mainFragment));
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
        else {
            createBleConnectionManager();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        for (int i = 0; i < permissions.length; ++i) {

            if (permissions[i].equals("android.permission.READ_PHONE_STATE") &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                /* Create Bluetooth LE connection manager */
                createBleConnectionManager();
            }
        }

    }

    public void createBleConnectionManager() {
        bleConnectionManager = new BleConnectionManager(getApplicationContext());
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

        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_handshake) {
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        } else if (id == R.id.nav_list) {
            HandshakeListFragment listFragment = new HandshakeListFragment();
            fragmentTransaction.replace(R.id.fragment_container, listFragment);
        } else if (id == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
        }

        fragmentTransaction.commit();

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
