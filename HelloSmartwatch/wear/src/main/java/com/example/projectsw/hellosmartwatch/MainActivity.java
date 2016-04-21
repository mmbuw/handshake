package com.example.projectsw.hellosmartwatch;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button mSendButton;

    private AccelerometerReader accelerometerReader;
    private DataTransmitter dataTransmitter;

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
                                byte randomNumber1 = (byte) (Math.random() * 100);
                                byte randomNumber2 = (byte) (Math.random() * 100);
                                byte randomNumber3 = (byte) (Math.random() * 100);
                                byte[] dataToSend = {randomNumber1, randomNumber2, randomNumber3};
                                dataTransmitter.requestTranscription(dataToSend);
                                return 0;
                            }
                        }.execute();
                    }
                });
            }
        });

        //Setup data transmitter
        dataTransmitter = new DataTransmitter(this.getApplicationContext());

        //Setup acceleration reader
        accelerometerReader = new AccelerometerReader(this.getApplicationContext(),
                                                      dataTransmitter);


    }




}
