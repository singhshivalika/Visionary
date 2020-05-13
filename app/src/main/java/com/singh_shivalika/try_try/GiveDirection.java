package com.singh_shivalika.try_try;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class GiveDirection extends AppCompatActivity implements View.OnTouchListener {

    ConstraintLayout tap_area;
    TextView box;
    Navigator navigator;
    CameraClass cameraClass;
    ArFragment arFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_givedirection);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragment);
        //arFragment.getView().setVisibility(View.INVISIBLE);
        ((ThisApplication)getApplication()).arFragment = arFragment;

        tap_area = findViewById(R.id.tap_area);
        box = findViewById(R.id.box);
        Intent i = getIntent();
        startCustomNav(i.getStringExtra("DATAPOINTS"));

        arFragment.getArSceneView().pauseAsync(AsyncTask.THREAD_POOL_EXECUTOR);
        tap_area.setOnTouchListener(this);
    }

    private void startCustomNav(String datapoints) {
        navigator = new Navigator(this,2,datapoints);
        ((ThisApplication)getApplication()).setGive_Instruction(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(!((ThisApplication)getApplication()).isGive_Instruction())break;
                    if(((ThisApplication)getApplication()).mode != 0)continue;
                    String t=  navigator.getUpdate(28.663067,77.452757);
                    Log.e("RESPONSE",t);
                    runOnUiThread(()-> { box.setText(t); });
                    MainActivity.voiceClass.speak(t);
                    try {
                        Thread.sleep(100 * t.length());
                    } catch (InterruptedException e) { }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(navigator!=null)
            navigator.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(navigator!=null)
            navigator.pause();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(()->{try { arFragment.getArSceneView().resume(); } catch (CameraNotAvailableException e) { e.printStackTrace(); }});
                    ((ThisApplication)getApplication()).objectDetector.cont = true;
                    ((ThisApplication)getApplication()).mode = 1;
                    ((ThisApplication)getApplication()).objectDetector.startDetecting();
                }
            }).start();
        }

        else if(event.getAction()==MotionEvent.ACTION_UP){
            ((ThisApplication)getApplication()).objectDetector.arfr.getArSceneView().getScene().removeOnUpdateListener(((ThisApplication)getApplication()).objectDetector);
            ((ThisApplication)getApplication()).objectDetector.cont = false;
            ((ThisApplication)getApplication()).mode = 0;
            arFragment.getArSceneView().pause();
        }
        return true;
    }
}
