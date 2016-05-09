package com.example.projectsw.thehandshakeapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;

import detection.FeatureExtractor;
import detection.FileOutputWriter;
import detection.HandshakeDetectedBluetoothAction;
import detection.HandshakeDetectedToastAction;
import util.BleAdvertisingCallback;
import util.BleScanCallback;
import util.HandshakeData;
import util.MessageData;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothLeAdvertiser bleAdvertiser;
    private AdvertiseSettings bleAdvSettings;
    private AdvertiseData bleAdvData1;
    private AdvertiseCallback bleAdvCallback;
    private ScanCallback bleScanCallback;

    boolean isScanActive = false;
    final Handler scanHandler = new Handler();

    final static int SCAN_PERIOD = 3000;
    final static public int BLE_TAG = 0x4343;

    static MessageData msgData;

    public static HashMap<String, HandshakeData> receivedHandshakes = new HashMap<>();

    private String uid;
    private int placeholder;

    Button shakeButton;
    Button settingsApplyButton;

    Toolbar toolbar;
    NavigationView navigationView;

    private TextView helloWorldTextView;
    private AccelerationDataReceiver serviceReceiver;
    private FileOutputWriter fileOutputWriter;
    private FileOutputWriter fileOutputWriterWithTime;
    private FeatureExtractor featureExtractor;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainFragment mainFragment = new MainFragment();
        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            msgData = new MessageData("1SRhxGT", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: CREATE PERMISSION FUNCTIONS

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    placeholder);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    placeholder);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    placeholder);
        }

        verifyStoragePermissions(this);

        uid = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        bluetoothAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE))
                .getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bleAdvCallback = new BleAdvertisingCallback();

        bleAdvSettings = new AdvertiseSettings.Builder().
                setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).
                setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).
                setConnectable(false).
                setTimeout(0).
                build();

        bleScanCallback = new BleScanCallback();

        bleAdvData1 = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(BLE_TAG, msgData.hash.getBytes()).build();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /* Register broadcast receiver */
        serviceReceiver = new AccelerationDataReceiver();
        IntentFilter intentSFilter = new IntentFilter("accelerationAction");
        registerReceiver(serviceReceiver, intentSFilter);

        /* Init file writers */
        fileOutputWriter = null;
        fileOutputWriterWithTime = null;

        /* Init feature extractor */
        featureExtractor = new FeatureExtractor(3,    // number of data columns
                                                1,    // index of major axis column
                                                1,    // samples for peak detection
                                                5.0f, // peak amplitude threshold
                                                15,   // peak repeat threshold
                                                0,    // moving average window width
                                                30,   // alternation time max diff
                                                5,    // alternation count detection threshold
                                                new HandshakeDetectedBluetoothAction(this));
                //new HandshakeDetectedToastAction(getApplicationContext()));
    }

    /* Requests the necessary storage permissions from the operating system */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void onScanButtonClick() {

        shakeButton = (Button) findViewById(R.id.shakeButton);

        if (!isScanActive) {
            startBle();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBle();
                }
            }, SCAN_PERIOD);
        } else {
            stopBle();
        }
    }

    public void onSettingsApplyButtonClick() {

        String newUrl = ((TextView) findViewById(R.id.setting_url_field)).getText().toString();

        try {
            MessageData newMsgData = new MessageData(newUrl, true);
            msgData = newMsgData;
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't convert URL.", Toast.LENGTH_SHORT);
        }


    }

    private void startBle() {
        if (!isScanActive) {
            bleAdvertiser.startAdvertising(bleAdvSettings, bleAdvData1, bleAdvCallback);
            bleScanner.startScan(bleScanCallback);
            isScanActive = true;
            shakeButton.setText(R.string.interrupt_scan_button_text);
        } else {
            Log.d("BLE", "Scan is already active!");
        }
    }

    private void stopBle() {
        if (isScanActive) {
            bleAdvertiser.stopAdvertising(bleAdvCallback);
            bleScanner.stopScan(bleScanCallback);
            isScanActive = false;
            shakeButton.setText(R.string.start_scan_button_text);
        } else {
            Log.d("BLE", "Scan is already inactive!");
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
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

    public String createNewFileWriters(String filename) {

        int unixTime = getCurrentUnixTimestamp();

        if (fileOutputWriter != null) {
            fileOutputWriter.closeStream();
            fileOutputWriterWithTime.closeStream();
        }

        if (filename.isEmpty()) {
            filename = "watch-" + unixTime + ".txt";
        }
        else {
            if (!filename.endsWith(".txt")) { filename += ".txt"; }
        }

        fileOutputWriter = new FileOutputWriter(filename);
        fileOutputWriterWithTime = new FileOutputWriter("timestamps-" + filename);
        return filename;
    }

    private int getCurrentUnixTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    /* Internal receiver class to get data from background service */
    public class AccelerationDataReceiver extends BroadcastReceiver {

        long lastMessageTimestamp = System.currentTimeMillis();
        int messageCount = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            float[] receivedValues = notificationData.getFloatArray("acceleration");

            /* Write data to current file if present */
            if (fileOutputWriter != null) {
                fileOutputWriter.writeToFile(receivedValues[0] + ", " +
                        receivedValues[1] + ", " +
                        receivedValues[2]);

                int unixTime = getCurrentUnixTimestamp();
                fileOutputWriterWithTime.writeToFile(unixTime + ", " +
                        receivedValues[0] + ", " +
                        receivedValues[1] + ", " +
                        receivedValues[2]);
            }

            /* Update FPS display */
            if (messageCount == 10) {
                long nowTime = System.currentTimeMillis();
                long diffTime = nowTime - lastMessageTimestamp;
                double diffSeconds = diffTime / 1000.0;
                //System.out.println(10.0 / diffSeconds + "");
                messageCount = 0;
                lastMessageTimestamp = nowTime;
            } else {
                ++messageCount;
            }

            /* Hand data over to feature extractor */
            featureExtractor.processDataRecord(receivedValues);


        }

    }

}
