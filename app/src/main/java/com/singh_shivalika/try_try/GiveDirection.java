package com.singh_shivalika.try_try;

import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GiveDirection extends AppCompatActivity {

    TextView box;
    Navigator navigator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_givedirection);
        box = findViewById(R.id.box);

        Intent i = getIntent();
        startCustomNav(i.getStringExtra("DATAPOINTS"));
    }

    private void startCustomNav(String datapoints) {
        navigator = new Navigator(this,2,datapoints);
        ((ThisApplication)getApplication()).setGive_Instruction(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(!((ThisApplication)getApplication()).isGive_Instruction())break;
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

}
