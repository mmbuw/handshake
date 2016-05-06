package com.example.projectsw.helloble;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// read this, bitch!
// http://developer.android.com/guide/topics/connectivity/bluetooth-le.html#setup
// http://www.truiton.com/2015/04/android-bluetooth-low-energy-ble-example/
// http://developer.radiusnetworks.com/2015/09/29/is-your-beacon-app-ready-for-android-6.html
public class MainActivity extends AppCompatActivity {

    static String D_TAG = "DEBUG";
    static String E_TAG = "ERROR";

    private int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;

    private TextView textView;
    private Button scanButton;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(D_TAG, "I'm just starting!");

        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textfield);

        mHandler = new Handler();

        checkBLE();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        scanButton = (Button) findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanButton.isClickable()){
                    scanButton.setClickable(false);
                    scanLeDevice(true);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.scan_in_progress, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<ScanFilter>();
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    scanButton.setClickable(true);
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
            scanButton.setClickable(true);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(D_TAG+":callbackType", String.valueOf(callbackType));
            Log.d(D_TAG+":result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            byte[] bytes = result.getScanRecord().getBytes();

            SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();

            String msg = "null";

            try {
                Log.d("MAN_DATA", manufacturerSpecificData.toString());
                msg = new String(manufacturerSpecificData.get(17219));
            }
            catch (Exception e){
                Log.e("MAN_DATA",e.toString());
            }

            textView.setText("");

            textView.append("Name:\t"           +btDevice.getName()+"\n");
            textView.append("Bytes:\t"          +bytes+"\n");
            textView.append("ManData:\t"        +manufacturerSpecificData+"\n");
            textView.append("Msg:\t"            +msg+"\n");
            textView.append("Address:\t"        +btDevice.getAddress()+"\n");
            textView.append("Type:\t"           +btDevice.getType()+"\n");
            textView.append("BondState:\t"      +btDevice.getBondState()+"\n");
            textView.append("RSSI:\t"           +result.getRssi()+"\n");
            textView.append("NanoTimeStamp:\t"  +result.getTimestampNanos()+"\n");
            textView.append("Time:\t"           +(new Date(System.currentTimeMillis()))+"\n\n");
            textView.append("=====================================\n\n");

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/"+msg));
            startActivity(browserIntent);

            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.d(D_TAG+":ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(E_TAG+":Scan Failed", "Error Code: " + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(D_TAG+":onConnectionChange", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(D_TAG+":onConnectionChange", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e(D_TAG+":onConnectionChange", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.d(D_TAG+":onServicesDiscovered", services.toString());

            gatt.readCharacteristic(services
                    .get(0).getCharacteristics()
                    .get(0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.d(D_TAG+":onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

    public void checkBLE() {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.ble_supported, Toast.LENGTH_SHORT).show();
        }

        //textView.setText(R.string.ble_supported);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Above Android M (6.0), we need to ask for permission at runtime if we want to run a
        // background task.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(D_TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
