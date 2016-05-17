package de.mobilemedia.thehandshakeapp.detection;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class InternalAccelerationListenerService extends Service implements SensorEventListener {

    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;


    private void sendDataToActivity(float[] data) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("accelerationAction");
        broadcastIntent.putExtra("acceleration", data);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sendDataToActivity(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
