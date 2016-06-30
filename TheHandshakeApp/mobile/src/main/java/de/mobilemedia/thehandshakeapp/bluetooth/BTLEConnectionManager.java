package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.mobile_core.Config;

public class BTLEConnectionManager extends BroadcastReceiver {

    public static final String LOG_TAG = "BTLETest";

    private WearableListenerService mParentService;

    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseSettings mAdvertiseSettings;
    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

    private boolean mScanning;
    private boolean mAdvertising;
    private Handler mHandler;
    private static HandshakeData myHandshakeData;

    public BTLEConnectionManager(WearableListenerService parentService) {

        mParentService = parentService;
        mHandler = new Handler();

        // Determine whether BLE is supported on the device
        if (!mParentService.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mParentService, "BTLE not supported", Toast.LENGTH_LONG).show();
            return;
        }

        // Get bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mParentService.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(mParentService, "BT not supported", Toast.LENGTH_LONG).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //mParentService.startActivityForResult(enableBtIntent, 1);
            System.out.println("BTLE NOT ENABLED");
        } else {
            initBluetoothObjects();
        }


    }

    public void initBluetoothObjects() {

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        mAdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(Config.BLE_ADVERTISE_MODE)
                .setTxPowerLevel(Config.BLE_ADVERTISE_TX_POWER_LVL)
                .setConnectable(false)
                .setTimeout(Config.BLE_SCAN_AND_ADVERTISE_PERIOD)
                .build();

        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();

        byte[] manufacturerDataToMatch = {0};
        byte[] dataMask = {0};

        mScanFilter = new ScanFilter.Builder()
                .setManufacturerData(Config.BLE_TAG, manufacturerDataToMatch, dataMask)
                .build();

    }

    public void advertiseBTLE(final boolean enable) {

        if (!mBluetoothAdapter.isEnabled()) {
            Toast toast = Toast.makeText(mParentService,
                    "Could not broadcast URL. Bluetooth is not enabled.",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Update myHandshakeData according to preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParentService);
        String longUrl = prefs.getString(mParentService.getString(R.string.url_pref_id),
                mParentService.getString(R.string.url_pref_default));
        String shortUrl = prefs.getString(mParentService.getString(R.string.url_short_pref_id),
                mParentService.getString(R.string.url_short_pref_default));
        myHandshakeData = new HandshakeData(shortUrl, longUrl);


        if (enable) {
            // Stops advertising after a pre-defined scan period
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    advertiseBTLE(false);
                }
            }, Config.BLE_SCAN_AND_ADVERTISE_PERIOD);

            String messageToTransceive = myHandshakeData.getMessageToTransceive();
            int unixTimestamp = Util.getCurrentUnixTimestamp();
            byte[] encryptedMessageBytes = Util.endecrypt(messageToTransceive.getBytes(), unixTimestamp);
            Log.d(LOG_TAG, "Plain text: " + myHandshakeData.getShortUrl());
            Log.d(LOG_TAG, "OUT DATA AT TIME " + unixTimestamp + " IS: ");
            Util.printByteArray(encryptedMessageBytes);

            AdvertiseData dataToAdvertise = new AdvertiseData.Builder()
                    .addManufacturerData(Config.BLE_TAG, encryptedMessageBytes)
                    .build();
            BTLEAdvertiseCallback callback = new BTLEAdvertiseCallback();

            mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, dataToAdvertise, callback);
            mAdvertising = true;
            Log.i(LOG_TAG, "Start advertising");

        } else {
            mBluetoothLeAdvertiser.stopAdvertising(new AdvertiseCallback() {} );
            mAdvertising = false;
            Log.i(LOG_TAG, "Stop advertising");
        }

    }

    public void scanBTLE(final boolean enable) {

        if (!mBluetoothAdapter.isEnabled()) {
            Toast toast = Toast.makeText(mParentService,
                    "Could not start scan. Bluetooth is not enabled.",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (enable) {
            // Stops scanning after a pre-defined scan period
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBTLE(false);
                }
            }, Config.BLE_SCAN_AND_ADVERTISE_PERIOD);

            mScanning = true;
            List<ScanFilter> filterList = new ArrayList<ScanFilter>();
            filterList.add(mScanFilter);
            mBluetoothLeScanner.startScan(filterList, mScanSettings, new BTLEScanCallback(){} );
            Log.i(LOG_TAG, "Start scanning");

        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(new ScanCallback() {});
            Log.i(LOG_TAG, "Stop scanning");
        }
    }


    /* Method of BroadcastReceiver to detect changes in Bluetooth activation state */
    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);

            if (state == BluetoothAdapter.STATE_ON) {
                initBluetoothObjects();
            }

        }

    }

}
