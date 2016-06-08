package de.mobilemedia.thehandshakeapp.bluetooth;

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
import android.util.Log;
import android.widget.Toast;

import de.mobilemedia.thehandshakeapp.mobile_core.Config;

import static de.mobilemedia.thehandshakeapp.mobile_core.Config.*;
import static de.mobilemedia.thehandshakeapp.mobile_core.Config.BLE_TAG;
import static de.mobilemedia.thehandshakeapp.mobile_core.Config.INITIAL_HANDSHAKE_LONGURL;
import static de.mobilemedia.thehandshakeapp.mobile_core.Config.INITIAL_HANDSHAKE_SHORTURL;

public class BleConnectionManager extends BroadcastReceiver {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothLeAdvertiser bleAdvertiser;
    private AdvertiseSettings bleAdvSettings;
    private AdvertiseData bleAdvData1;
    private AdvertiseCallback bleAdvCallback;
    private ScanCallback bleScanCallback;

    private Context context;
    private boolean isScanActive = false;

    private static HandshakeData myHandshakeData;

    public BleConnectionManager(Context context) {
        this.context = context;
        myHandshakeData = new HandshakeData(INITIAL_HANDSHAKE_SHORTURL, INITIAL_HANDSHAKE_LONGURL);
        Log.d("BLE", "new ble connection manager");

        bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        if (bluetoothAdapter.isEnabled()) {
            initBluetoothObjects();
        }

    }

    public void initBluetoothObjects() {

        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bleAdvCallback = new BleAdvertisingCallback();

        bleAdvSettings = new AdvertiseSettings.Builder().
                setAdvertiseMode(ADVERTISE_MODE).
                setTxPowerLevel(ADVERTISE_TX_POWER_LVL).
                setConnectable(false).
                setTimeout(0).
                build();

        bleScanCallback = new BleScanCallback();

        bleAdvData1 = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(BLE_TAG, myHandshakeData.getHash().getBytes()).build();
    }

    public void startBle() {

        // Abort if bluetooth is not enabled
        if (!bluetoothAdapter.isEnabled()) {
            Toast toast = Toast.makeText(context,
                                         "Could not broadcast URL. Bluetooth is not enabled.",
                                         Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!isScanActive) {
            bleAdvertiser.startAdvertising(bleAdvSettings, bleAdvData1, bleAdvCallback);
            bleScanner.startScan(bleScanCallback);
            isScanActive = true;
        } else {
            Log.d("BLE", "scan is still active");
        }
    }

    public void stopBle() {
        if (isScanActive) {
            bleAdvertiser.stopAdvertising(bleAdvCallback);
            bleScanner.stopScan(bleScanCallback);
            isScanActive = false;
        } else {
            Log.d("BLE", "scan is already inactive");
        }
    }

    public boolean isScanActive() {
        return isScanActive;
    }

    public void setMyHandshake(HandshakeData handshakeData) {
        myHandshakeData = handshakeData;
        bleAdvData1 = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(BLE_TAG, myHandshakeData.getHash().getBytes()).build();
    }

    public static HandshakeData getMyHandshakeData() {
        return myHandshakeData;
    }

    public String getLongUrl() {
        return myHandshakeData.getLongUrl();
    }


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
