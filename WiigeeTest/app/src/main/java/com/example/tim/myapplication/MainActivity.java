package com.example.tim.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.device.AndroidDevice;
import org.wiigee.device.Device;

public class MainActivity extends AppCompatActivity {

    private AndroidWiigee mWiigee;
    private AndroidDevice mDevice;

    private Button mButStartTraining;
    private Button mButStopTraining;
    private Button mButCloseGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWiigee();
        initButtons();
    }

    public void initButtons() {
        mButStartTraining = (Button) findViewById(R.id.butStartTraining);
        mButStartTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.startTraining();
            }
        });

        mButStopTraining = (Button) findViewById(R.id.butStopTraining);
        mButStopTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.stopTraining();
            }
        });

        mButCloseGesture = (Button) findViewById(R.id.butCloseGesture);
        mButCloseGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.closeGesture();
            }
        });
    }

    public void initWiigee() {
        mWiigee = new AndroidWiigee(this);
        mDevice = mWiigee.getDevice();
    }
}
