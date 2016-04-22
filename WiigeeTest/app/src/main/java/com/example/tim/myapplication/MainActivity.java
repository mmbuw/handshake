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
    private TextView mTextStatus;

    private int mNumGesturesStored;
    private int mNumTrainingSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumGesturesStored = 0;
        mNumTrainingSamples = 0;

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
                mTextStatus.setText("Training");
            }
        });

        mButStopTraining = (Button) findViewById(R.id.butStopTraining);
        mButStopTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.stopTraining();
                mTextStatus.setText("Stored training example " + mNumTrainingSamples++);
            }
        });

        mButCloseGesture = (Button) findViewById(R.id.butCloseGesture);
        mButCloseGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.closeGesture();
                mTextStatus.setText("Save gesture with ID " + mNumGesturesStored++);
                mNumTrainingSamples = 0;
            }
        });

        mButStartRecognition = (Button) findViewById(R.id.butStartRecognition);
        mButStartRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.startRecognition();
                mTextStatus.setText("Recording");
            }
        });

        mButStopRecognition = (Button) findViewById(R.id.butStopRecognition);
        mButStopRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice.stopRecognition();
                mTextStatus.setText("Ready");
            }
        });
    }

    public void initTextViews() {
        mTextRecognizedGesture = (TextView) findViewById(R.id.textRecognizedGesture);
        mTextRecognizedGesture.setText("Recognized Gesture: None");

        mTextStatus = (TextView) findViewById(R.id.textStatus);
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
