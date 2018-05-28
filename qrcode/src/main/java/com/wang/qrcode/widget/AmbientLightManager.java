package com.wang.qrcode.widget;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.wang.qrcode.camera.CameraManager;


/**
 * Detects ambient light and switches on the front light when very dark, and off again when sufficiently light.
 */
final class AmbientLightManager implements SensorEventListener {

    private static final float TOO_DARK_LUX = 1.0f;
    private static final float BRIGHT_ENOUGH_LUX = 450.0f;

    private final Context context;
    private CameraManager cameraManager;
    private Sensor lightSensor;

    AmbientLightManager(Context context) {
        this.context = context;
    }

    void start(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    void stop() {
        cameraManager = null;
        if (lightSensor != null) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorManager.unregisterListener(this, lightSensor);
            }
            lightSensor = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambientLightLux = sensorEvent.values[0];
        if (cameraManager != null) {
            if (ambientLightLux <= TOO_DARK_LUX) {
                cameraManager.setTorch(true);
            } else if (ambientLightLux >= BRIGHT_ENOUGH_LUX) {
                cameraManager.setTorch(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

}
