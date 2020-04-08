package com.singh_shivalika.try_try;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import static android.provider.Settings.System.getString;

public class VoiceClass {

    static Context current_context;

    static TextToSpeech tts;
    boolean initialized = false;

    VoiceClass(Context context){
        this.current_context = context;

        tts = new TextToSpeech(current_context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = tts.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                    initialized = true;
                } else {
                    Toast.makeText(current_context.getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        while(!initialized);
    }

    public static void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

        try {
            ((MainActivity)current_context).startActivityForResult(intent, 101);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(((MainActivity)current_context).getApplicationContext(), "Speech Recognition not working", Toast.LENGTH_SHORT).show();
        }
    }

    public static void speak(String string) {
        tts.speak(string,TextToSpeech.QUEUE_FLUSH,null);
    }
}