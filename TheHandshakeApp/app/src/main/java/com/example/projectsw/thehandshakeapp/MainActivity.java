package com.example.projectsw.thehandshakeapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import java.util.HashMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainFragment mainFragment = new MainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction
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

        uid = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        bluetoothAdapter = ((android.bluetooth.BluetoothManager)getSystemService(BLUETOOTH_SERVICE))
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
    }

    public void onScanButtonClick(){

        shakeButton = (Button) findViewById(R.id.shakeButton);

        if (!isScanActive){
            startBle();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBle();
                }
            }, SCAN_PERIOD);
        }  else {
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

    private void startBle(){
        if (!isScanActive){
            bleAdvertiser.startAdvertising(bleAdvSettings, bleAdvData1, bleAdvCallback);
            bleScanner.startScan(bleScanCallback);
            isScanActive = true;
            shakeButton.setText(R.string.interrupt_scan_button_text);
        }
        else{
            Log.d("BLE","Scan is already active!");
        }
    }

    private void stopBle(){
        if (isScanActive){
            bleAdvertiser.stopAdvertising(bleAdvCallback);
            bleScanner.stopScan(bleScanCallback);
            isScanActive = false;
            shakeButton.setText(R.string.start_scan_button_text);
        }
        else{
            Log.d("BLE","Scan is already inactive!");
        }
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
            android.support.v4.app.FragmentTransaction fragmentTransaction
                    = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_list) {

        } else if (id == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction
                    = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
