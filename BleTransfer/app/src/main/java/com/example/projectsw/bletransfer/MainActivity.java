package com.example.projectsw.bletransfer;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothLeAdvertiser bleAdvertiser;
    private AdvertiseSettings bleAdvSettings;
    private AdvertiseData bleAdvData1;
    private AdvertiseCallback bleAdvCallback;
    private ScanCallback bleScanCallback;

    private boolean isScanActive = false;
    final Handler scanHandler = new Handler();

    final static int SCAN_PERIOD = 3000;
    final static public int BLE_TAG = 0x4343;

    static MessageData msgData;

    public static HashMap<String, HandshakeData> receivedHandshakes = new HashMap<>();

    private String uid;
    private int placeholder;

    Button shakeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        shakeButton = (Button)findViewById(R.id.shakeButton);

        shakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

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

}
