package com.example.tim.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.device.AndroidDevice;
import org.wiigee.device.Device;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

public class MainActivity extends AppCompatActivity implements GestureListener {

    private AndroidWiigee mWiigee;
    private AndroidDevice mDevice;

    private Button mButStartTraining;
    private Button mButStopTraining;
    private Button mButCloseGesture;
    private Button mButStartRecognition;
    private Button mButStopRecognition;
    private TextView mTextRecognizedGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWiigee();
        initButtons();
        initTextViews();
    }

    public void initWiigee() {
        mWiigee = new AndroidWiigee(this);
        mDevice = mWiigee.getDevice();
        this.mDevice.addGestureListener(this);
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

        mButStartRecognition = (Button) findViewById(R.id.butStartRecognition);
        mButStartRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.startRecognition();
            }
        });

        mButStopRecognition = (Button) findViewById(R.id.butStopRecognition);
        mButStopRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.stopRecognition();
            }
        });
    }

    public void initTextViews() {
        mTextRecognizedGesture = (TextView) findViewById(R.id.textRecognizedGesture);
        mTextRecognizedGesture.setText("Recognized Gesture: None");
    }


    @Override
    public void gestureReceived(GestureEvent event) {

        if (event.isValid()) {
            mTextRecognizedGesture.setText("Recognized Gesture: " + event.getId());
        }
        else {
            mTextRecognizedGesture.setText("Recognized Gesture: None");
        }

    }
}
