package de.mobilemedia.thehandshakeapp.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.mobilemedia.thehandshakeapp.mobile_core.Config;

public class BTLEConnectionManager {

    public static final String LOG_TAG = "BTLETest";

    private Activity mParentActivity;

    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseSettings mAdvertiseSettings;
    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

    private Button mButtonToGreyOut;

    private boolean mScanning;
    private boolean mAdvertising;
    private Handler mHandler;
    private static HandshakeData myHandshakeData;

    public BTLEConnectionManager(Activity parentActivity) {

        mParentActivity = parentActivity;
        mHandler = new Handler();
        myHandshakeData = new HandshakeData(Config.INITIAL_HANDSHAKE_SHORTURL,
                                            Config.INITIAL_HANDSHAKE_LONGURL);

        // Determine whether BLE is supported on the device
        if (!mParentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mParentActivity, "BTLE not supported", Toast.LENGTH_LONG).show();
            return;
        }

        // Get bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mParentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(mParentActivity, "BT not supported", Toast.LENGTH_LONG).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mParentActivity.startActivityForResult(enableBtIntent, 1);
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        mAdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(Config.BLE_ADVERTISE_PERIOD)
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

        if (enable) {
            // Stops advertising after a pre-defined scan period
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    advertiseBTLE(false);
                }
            }, Config.BLE_ADVERTISE_PERIOD);

            String messageToTransceive = myHandshakeData.getMessageToTransceive();
            int unixTimestamp = Util.getCurrentUnixTimestamp();
            byte[] encryptedMessageBytes = Util.endecrypt(messageToTransceive.getBytes(), unixTimestamp);
            Log.d(LOG_TAG, "OUT DATA AT TIME " + unixTimestamp + " IS: ");
            Util.printByteArray(encryptedMessageBytes);

            AdvertiseData dataToAdvertise = new AdvertiseData.Builder()
                    .addManufacturerData(Config.BLE_TAG, encryptedMessageBytes)
                    .setIncludeDeviceName(true)
                    .build();
            BTLEAdvertiseCallback callback = new BTLEAdvertiseCallback();

            mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, dataToAdvertise, callback);
            mAdvertising = true;

            if (mButtonToGreyOut != null)
                mButtonToGreyOut.setEnabled(false);

            Log.i(LOG_TAG, "Start advertising");

        } else {

            mBluetoothLeAdvertiser.stopAdvertising(new AdvertiseCallback() {} );
            mAdvertising = false;

            if (mButtonToGreyOut != null && !mScanning)
                mButtonToGreyOut.setEnabled(true);

            Log.i(LOG_TAG, "Stop advertising");

        }

    }

    public void scanBTLE(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBTLE(false);
                }
            }, Config.BLE_SCAN_PERIOD);

            mScanning = true;
            List<ScanFilter> filterList = new ArrayList<ScanFilter>();
            filterList.add(mScanFilter);
            mBluetoothLeScanner.startScan(filterList, mScanSettings, new BTLEScanCallback(){} );

            if (mButtonToGreyOut != null)
                mButtonToGreyOut.setEnabled(false);

            Log.i(LOG_TAG, "Start scanning");

        } else {

            mScanning = false;
            mBluetoothLeScanner.stopScan(new ScanCallback() {});

            if (mButtonToGreyOut != null && !mAdvertising)
                mButtonToGreyOut.setEnabled(true);

            Log.i(LOG_TAG, "Stop scanning");

        }
    }

    public void setMyHandshake(HandshakeData handshakeData) {
        myHandshakeData = handshakeData;
    }

    public static HandshakeData getMyHandshakeData() {
        return myHandshakeData;
    }

    public String getLongUrl() {
        return myHandshakeData.getLongUrl();
    }

    public void setButtonToGreyOut(Button buttonToGreyOut) {
        mButtonToGreyOut = buttonToGreyOut;
    }

}
