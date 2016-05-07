package mobile_core;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private Button mNewFileButton;
    private TextView mStatusTextView;
    private EditText mFileNameEditText;

    private AccelerationDataReceiver serviceReceiver;
    private FileOutputWriter fileOutputWriter;
    private FileOutputWriter fileOutputWriterWithTime;
    private FeatureExtractor featureExtractor;

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

        mStatusTextView = (TextView) findViewById(R.id.textStatus);
        mNewFileButton = (Button) findViewById(R.id.butNewFile);
        mNewFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFileWriters();
            }
        });
        mFileNameEditText = (EditText) findViewById(R.id.inputFileName);

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
                                                new HandshakeDetectedToastAction(getApplicationContext()));

        /* Get permissions */
        verifyStoragePermissions(this);

        /* Internal accelerometer data input for debugging */
        //Intent intent = new Intent(getApplicationContext(), InternalAccelerationListenerService.class );
        //startService(intent);
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

    public void createNewFileWriters() {

        int unixTime = getCurrentUnixTimestamp();

        if (fileOutputWriter != null) {
            fileOutputWriter.closeStream();
            fileOutputWriterWithTime.closeStream();
        }

        String filename = mFileNameEditText.getText().toString();
        if (filename.isEmpty()) {
            filename = "watch-" + unixTime + ".txt";
        }
        else {
            if (!filename.endsWith(".txt")) { filename += ".txt"; }
            mFileNameEditText.setText("");
        }

        fileOutputWriter = new FileOutputWriter(filename);
        fileOutputWriterWithTime = new FileOutputWriter("timestamps-" + filename);
        mStatusTextView.setText("Current file created at time " + unixTime + " with name " + filename);
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
            float[] receivedValues  = notificationData.getFloatArray("acceleration");

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
                mTextView.setText(10.0/diffSeconds + "");
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
