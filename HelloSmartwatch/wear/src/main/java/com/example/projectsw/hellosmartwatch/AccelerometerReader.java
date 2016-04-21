package com.example.projectsw.hellosmartwatch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.nio.ByteBuffer;

public class AccelerometerReader implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DataTransmitter mDataTransmitter;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    public AccelerometerReader(Context context, DataTransmitter dataTransmitter) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mDataTransmitter = dataTransmitter;
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

        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.putFloat(linear_acceleration[0]);
        byteBuffer.putFloat(linear_acceleration[1]);
        byteBuffer.putFloat(linear_acceleration[2]);
        mDataTransmitter.requestTranscription(byteBuffer.array());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
