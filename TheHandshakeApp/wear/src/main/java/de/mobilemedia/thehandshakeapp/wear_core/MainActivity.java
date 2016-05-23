package de.mobilemedia.thehandshakeapp.wear_core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import wear_core.R;

public class MainActivity extends Activity {

    private TextView mTextView;
    private Button mSendButton;

    private String[] mStatusData = {"Not transmitting", "Transmitting"};
    private int mTransmissionStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Create user interface
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mSendButton = (Button) stub.findViewById(R.id.sendButton);

                mSendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mTransmissionStatus == 0) {
                            Intent intent = new Intent(getApplicationContext(), AccelerometerService.class);
                            startService(intent);
                        }
                        else {
                            Intent intent = new Intent(getApplicationContext(), AccelerometerService.class);
                            stopService(intent);
                        }

                        mTransmissionStatus = (mTransmissionStatus + 1) % 2;
                        mTextView.setText(mStatusData[mTransmissionStatus]);
                    }

                });

                mTextView = (TextView) stub.findViewById(R.id.textStatus);
            }
        });

    }




}
