package de.mobilemedia.thehandshakeapp.wear_core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DataTransmitter mDataTransmitter;

    private boolean mActiveStatus;
    private PowerManager.WakeLock mWakeLock;

    private float[] gravity;
    private float[] linear_acceleration;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mDataTransmitter = new DataTransmitter(this);
        mActiveStatus = true;
        gravity = new float[3];
        linear_acceleration = new float[3];

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Stay awake");
        mWakeLock.acquire();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mActiveStatus = false;
        mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void transmitSensorData(float[] sensorData) {
        // Send sensor data via the data transmitter
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream ds = new DataOutputStream(bas);

        try {

            for (float f : sensorData)
                ds.writeFloat(f);

        } catch (IOException ioe) {}

        mDataTransmitter.requestTranscription(bas.toByteArray());
    }

    public void onSensorChanged(SensorEvent event) {

        if (mActiveStatus) {

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

            transmitSensorData(linear_acceleration);
        }
    }


}
