package de.mobilemedia.thehandshakeapp.wear_core;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import wear_core.R;

public class MainActivity extends Activity {

    private TextView mTextView;
    private Button mSendButton;

    private AccelerometerHandler accelerometerHandler;
    private DataTransmitter dataTransmitter;

    private String[] statusData = {"Not transmitting", "Transmitting"};
    private int currentStatus = 0;

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
                        new AsyncTask<Void, Void, Integer>() {
                            protected Integer doInBackground(Void... params) {
                                accelerometerHandler.switchTransmissionMode();
                                return 0;
                            }
                        }.execute();
                        currentStatus = (currentStatus + 1) % 2;
                        mTextView.setText(statusData[currentStatus]);
                    }
                });

                mTextView = (TextView) stub.findViewById(R.id.textStatus);
            }
        });

        //Setup data transmitter
        dataTransmitter = new DataTransmitter(this.getApplicationContext());

        //Setup acceleration reader
        accelerometerHandler = new AccelerometerHandler(this.getApplicationContext(),
                                                        dataTransmitter);


    }




}
