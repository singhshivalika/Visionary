package com.singh_shivalika.try_try;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.VIBRATOR_SERVICE;

public class Vibration {

    Context appcontext;
    Vibrator vibrator;

    Vibration(Context appcontext) {
        this.appcontext = appcontext;
        vibrator = (Vibrator) ((AppCompatActivity) appcontext).getSystemService(VIBRATOR_SERVICE);
    }

    public void vibrate(int time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(time*1000, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            vibrator.vibrate(time*1000);
    }
}
