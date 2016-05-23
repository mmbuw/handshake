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
import android.os.Vibrator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AccelerometerService extends Service implements SensorEventListener {

    public static float[] GRAVITY_START_EVENT_VALUES = {20.0f, 0.0f, 0.0f};
    public static float[] GRAVITY_END_EVENT_VALUES = {-20.0f, 0.0f, 0.0f};
    public static int NUM_NON_ORIENTED_SAMPLES_FOR_END = 50;
    public static int NUM_ORIENTED_SAMPLES_FOR_START = 10;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Vibrator mVibrator;
    private DataTransmitter mDataTransmitter;

    private boolean mActiveStatus;
    private PowerManager.WakeLock mWakeLock;

    private float[] gravity = new float[3];
    private float gravityAlpha = 0.9f;
    private boolean orientationAllowsTransmission = false;
    private int numConsecutiveSamplesNotInHandshakeOrientation = 0;
    private int numConsecutiveSamplesInHandshakeOrientation = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mDataTransmitter = new DataTransmitter(this);
        mActiveStatus = true;

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Stay awake");
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

            // compute gravity out of retrieved sensor values
            gravity[0] = gravityAlpha * gravity[0] + (1 - gravityAlpha) * event.values[0];
            gravity[1] = gravityAlpha * gravity[1] + (1 - gravityAlpha) * event.values[1];
            gravity[2] = gravityAlpha * gravity[2] + (1 - gravityAlpha) * event.values[2];

            // if most of the gravity is pointing to -y, the palm is facing to the left
            boolean inHandShakeOrientation = (gravity[1] < -7.0f);

            // update number of consecutive non-palm-left samples
            if (inHandShakeOrientation) {
                numConsecutiveSamplesNotInHandshakeOrientation = 0;
                numConsecutiveSamplesInHandshakeOrientation++;
            }
            else {
                numConsecutiveSamplesNotInHandshakeOrientation++;
                numConsecutiveSamplesInHandshakeOrientation = 0;
            }

            // detect if a palm-left-start-event or a palm-left-end-event occurred
            if (    !orientationAllowsTransmission &&
                    numConsecutiveSamplesInHandshakeOrientation > NUM_ORIENTED_SAMPLES_FOR_START) {

                transmitSensorData(GRAVITY_START_EVENT_VALUES);
                mVibrator.vibrate(250);
                orientationAllowsTransmission = true;

            }
            else if ( orientationAllowsTransmission &&
                      numConsecutiveSamplesNotInHandshakeOrientation > NUM_NON_ORIENTED_SAMPLES_FOR_END) {

                transmitSensorData(GRAVITY_END_EVENT_VALUES);
                mVibrator.vibrate(500);
                orientationAllowsTransmission = false;

            }

            // transmit data if gravity classification above allows to do so
            if (orientationAllowsTransmission) {
                transmitSensorData(event.values);
            }

        }
    }


}
