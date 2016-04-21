package com.example.projectsw.hellosmartwatch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AccelerometerHandler implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DataTransmitter mDataTransmitter;
    private boolean mTransmissionActivated;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    public AccelerometerHandler(Context context, DataTransmitter dataTransmitter) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mDataTransmitter = dataTransmitter;
        mTransmissionActivated = false;
    }

    public void switchTransmissionMode() {
        mTransmissionActivated = !mTransmissionActivated;
    }

    private void transmitCurrentSensorData() {
        // Send sensor data via the data transmitter
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream ds = new DataOutputStream(bas);

        try {

            for (float f : linear_acceleration)
                ds.writeFloat(f);

        } catch (IOException ioe) {}

        mDataTransmitter.requestTranscription(bas.toByteArray());
    }

    public void onSensorChanged(SensorEvent event) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        // Transmit the retrieved sensor data if activated
        if (mTransmissionActivated) {
            transmitCurrentSensorData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
