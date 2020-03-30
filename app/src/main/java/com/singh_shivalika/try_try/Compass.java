package com.singh_shivalika.try_try;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

class Compass implements SensorEventListener {

    private SensorManager SensorManage;
    private Context appcontext;
    private SensorManager sensorManager;
    private float DegreeStart = 0f;

    public float degree;


    Compass(Context appcontext){
        this.appcontext = appcontext;
        sensorManager = (SensorManager) appcontext.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
    }

    public void pause(){
        sensorManager.unregisterListener(this);
    }

    public void resume() {
        //sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
