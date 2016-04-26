package com.example.projectsw.hellosmartwatch;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private AccelerationDataReceiver serviceReceiver;
    private FileOutputWriter fileOutputWriter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.mainTextView);

        Intent intent = getIntent();
        byte[] intentData = intent.getByteArrayExtra("TEST_MESSAGE");

        if (intentData != null) {
            if (intentData.length == 3) {
                mTextView.setText(intentData[0] +  ", " + intentData[1] + ", " + intentData[2]);
            }
            else if (intentData.length == 12)
            {
                ByteBuffer byteBuffer = ByteBuffer.wrap(intentData);
                mTextView.setText(byteBuffer.getFloat(0) + ", " +
                                  byteBuffer.getFloat(1) + ", " +
                                  byteBuffer.getFloat(2));
            }
        }

        /* Register broadcast receiver */
        serviceReceiver = new AccelerationDataReceiver();
        IntentFilter intentSFilter = new IntentFilter("AccelerationDataAction");
        registerReceiver(serviceReceiver, intentSFilter);

        /* Init FileOutputWriter */
        verifyStoragePermissions(this);
        fileOutputWriter = new FileOutputWriter(getApplicationContext(), "recording.txt");

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

    /* Internal receiver class to get data from background service */
    public class AccelerationDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            float[] receivedValues  = notificationData.getFloatArray("AccelerationData");
            /*System.out.println(receivedValues[0] + ", " +
                               receivedValues[1] + ", " +
                               receivedValues[2]);*/
            fileOutputWriter.writeToFile(receivedValues[0] + ", " +
                                         receivedValues[1] + ", " +
                                         receivedValues[2]);
        }

    }


}
