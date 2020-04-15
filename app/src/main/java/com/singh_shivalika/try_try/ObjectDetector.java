package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.util.ArrayList;
import java.util.List;

public class ObjectDetector implements OnSuccessListener<List<FirebaseVisionImageLabel>> {

    private final static int MARGIN = 10;
    public boolean cont = false;

    VoiceClass voiceClass;

    FirebaseVisionObjectDetector objectDetector;
    FirebaseVisionImageLabeler labeler;

    Context appcontext;

    ObjectDetector(Context appcontext){
        this.appcontext = appcontext;
        FirebaseVisionObjectDetectorOptions options = new FirebaseVisionObjectDetectorOptions.Builder().setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE).enableClassification().build();
        FirebaseApp.initializeApp(appcontext);
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
    }

    public void startDetecting(){
        ArFragment arfr = ((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).arFragment;
        try {
            detect(arfr.getArSceneView().getArFrame().acquireCameraImage());
        }catch (Exception e){
            Log.e("NOT_AVAI","Not available, initiating"+e.toString());
            if(((ThisApplication)((AppCompatActivity)appcontext).getApplication()).mode == 1)
                startDetecting(); }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public void detect(Image img) {
        detectedObjects.clear();
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(img,0);
        img.close();
        objectDetector.processImage(image).addOnSuccessListener(detectedObjects -> {

            for(FirebaseVisionObject o : detectedObjects){
                Rect extraRect = new Rect( Math.max(0,o.getBoundingBox().left-MARGIN),Math.max(0,o.getBoundingBox().top-MARGIN),Math.min(o.getBoundingBox().right+MARGIN,image.getBitmap().getWidth()),Math.min(o.getBoundingBox().bottom+MARGIN,image.getBitmap().getHeight()));
                labeler.processImage(FirebaseVisionImage.fromBitmap(Bitmap.createBitmap( image.getBitmap() ,extraRect.left,extraRect.top,extraRect.width(),extraRect.height()))).addOnSuccessListener(this);
            }
            say();
        });
    }

    List<String> detectedObjects = new ArrayList<>();
    @Override
    public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
        StringBuilder sb = new StringBuilder();
        double confidence = 0;
        String product = "";
        for(FirebaseVisionImageLabel l : firebaseVisionImageLabels){
            Log.e("LOL",l.getText()+" "+l.getConfidence());
            if(l.getConfidence()>0.80) {
                if (!detectedObjects.contains(l.getText()))
                    detectedObjects.add(l.getText());
            }
        }
    }

    private void say() {
        if(detectedObjects.size()==0){startDetecting(); return;}
        StringBuilder sb = new StringBuilder();
        for(String str: detectedObjects)
            sb.append(str+" ");

        voiceClass.speak(sb.toString());
        try {
            Thread.sleep(100 * sb.toString().length());
        }catch (Exception e){ }

        if(((ThisApplication)((AppCompatActivity)appcontext).getApplication()).mode == 1)
            startDetecting();
    }

    public void setVoiceClass(VoiceClass voiceClass) {
        this.voiceClass = voiceClass;
    }
}
