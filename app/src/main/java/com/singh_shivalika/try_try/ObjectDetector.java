package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetector implements Scene.OnUpdateListener {

    private final static int MARGIN = 10;
    public boolean cont = false;

    VoiceClass voiceClass;

    FirebaseVisionObjectDetector objectDetector;
    FirebaseVisionImageLabeler labeler;

    Context appcontext;
    ArFragment arfr;

    ObjectDetector(Context appcontext){
        this.appcontext = appcontext;
        arfr = ((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).arFragment;
        FirebaseVisionObjectDetectorOptions options = new FirebaseVisionObjectDetectorOptions.Builder().setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE).enableClassification().build();
        FirebaseApp.initializeApp(appcontext);
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
    }

    public void startDetecting(){
        arfr = ((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).arFragment;
        if(arfr!=null)
            arfr.getArSceneView().getScene().addOnUpdateListener(this);
        else if(((ThisApplication)((AppCompatActivity)appcontext).getApplication()).mode == 1)
            startDetecting();
    }

    Map<String,DetectedObject> detected_objs = new HashMap<>();

    public void detect(Image img) {
        detected_objs.clear();

        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(img,0);
        img.close();
        objectDetector.processImage(image).addOnSuccessListener(detectedObjects -> {

            for(FirebaseVisionObject o : detectedObjects) {
                Rect extraRect = new Rect(Math.max(0, o.getBoundingBox().left - MARGIN), Math.max(0, o.getBoundingBox().top - MARGIN), Math.min(o.getBoundingBox().right + MARGIN, image.getBitmap().getWidth()), Math.min(o.getBoundingBox().bottom + MARGIN, image.getBitmap().getHeight()));
                labeler.processImage(FirebaseVisionImage.fromBitmap(Bitmap.createBitmap(image.getBitmap(), extraRect.left, extraRect.top, extraRect.width(), extraRect.height()))).addOnSuccessListener(firebaseVisionImageLabels -> {

                    double confidence = 0;
                    String product = "";
                    DetectedObject object = new DetectedObject();

                    for (FirebaseVisionImageLabel l : firebaseVisionImageLabels) {
                        Log.e("LOL", l.getText() + " " + l.getConfidence());
                        if (l.getConfidence() > 0.85) {
                            product = l.getText();
                            confidence = l.getConfidence();
                            object.setConfidence(confidence);
                            object.setProduct(product);
                            object.setX_Y((double) (extraRect.left + extraRect.right) / 2, (double) (extraRect.top + extraRect.bottom) / 2);
                            detected_objs.put(product, object);
                        }
                    }
                });
            }
        });
    }

    private void getDistance() {
        ArFragment arFragment =  ((ThisApplication) ((MainActivity)appcontext).getApplication()).arFragment;
        if(arFragment==null)return;
    }

    private void say() {
        if(detected_objs.size()==0){startDetecting(); return;}
        StringBuilder sb = new StringBuilder();
        for(String str: detected_objs.keySet()) {
            sb.append(detected_objs.get(str).getProduct() + " ");
            if(detected_objs.get(str).getDistance()!=0)sb.append(" at "+detected_objs.get(str).getDistance()+" meters");
        }
        Log.e("OUTCOME",sb.toString());
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

    boolean toDO = true;
    @Override
    public void onUpdate(FrameTime frameTime) {
        if(!toDO)return;

        toDO = false;
        Session arSession = arfr.getArSceneView().getSession();
        if(arSession==null)return;

        Frame f=null;
        try { f = arSession.update(); } catch (CameraNotAvailableException e) {
            if (((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).mode == 1)
                startDetecting();
        }

        if(f==null)return;

        try {
            detect(f.acquireCameraImage());
        }catch (NotYetAvailableException e){ }

        MotionEvent motionEvent = null;

        for(DetectedObject o : detected_objs.values()){
            motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis()+10, MotionEvent.ACTION_UP, (int) o.getX(), (int) o.getY(), 0);
            Collection<HitResult> results =  f.hitTest(motionEvent.getX(),motionEvent.getY());
            if(results.size()==0)continue;
            HitResult currentHit = (HitResult) results.toArray()[0];
            o.setDistance(currentHit.getDistance());
            Log.e("SET",String.valueOf(currentHit.getDistance()));
        }
        say();
        toDO = true;
    }
}