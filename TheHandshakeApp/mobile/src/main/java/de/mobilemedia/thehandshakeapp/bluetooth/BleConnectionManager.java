package de.mobilemedia.thehandshakeapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BleConnectionManager {

    public static final int SCAN_PERIOD = 3000;
    public static final int BLE_TAG = 0x4343;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothLeAdvertiser bleAdvertiser;
    private AdvertiseSettings bleAdvSettings;
    private AdvertiseData bleAdvData1;
    private AdvertiseCallback bleAdvCallback;
    private ScanCallback bleScanCallback;

    boolean isScanActive = false;

    static MessageData msgData;

    private String uid;


    public BleConnectionManager(Context context) {
        uid = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        try {
            setMessageData(new MessageData("1SRhxGT", false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
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

    public void startBle() {
        if (!isScanActive) {
            bleAdvertiser.startAdvertising(bleAdvSettings, bleAdvData1, bleAdvCallback);
            bleScanner.startScan(bleScanCallback);
            isScanActive = true;
            //shakeButton.setText(R.string.interrupt_scan_button_text);
        } else {
            Log.d("BLE", "Scan is already active!");
        }
    }

    public void stopBle() {
        if (isScanActive) {
            bleAdvertiser.stopAdvertising(bleAdvCallback);
            bleScanner.stopScan(bleScanCallback);
            isScanActive = false;
            //shakeButton.setText(R.string.start_scan_button_text);
        } else {
            Log.d("BLE", "Scan is already inactive!");
        }
    }

    public boolean isScanActive() {
        return isScanActive;
    }

    public void setMessageData(MessageData newMsgData) {
        msgData = newMsgData;
    }

    public String getCurrentUrl() {
        return msgData.url.toString();
    }

}
