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
import androidx.camera.core.impl.ImageOutputConfig;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

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
    FirebaseVisionTextRecognizer textRecognizer;
    FirebaseVisionImageLabeler labeler;

    Context appcontext;
    ArFragment arfr;

    ObjectDetector(Context appcontext){
        this.appcontext = appcontext;
        arfr = ((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).arFragment;

        /*FirebaseVisionObjectDetectorOptions options = new FirebaseVisionObjectDetectorOptions.Builder().setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE).enableClassification().build();
        FirebaseApp.initializeApp(appcontext);
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
*/    }

    public void startDetecting(){
        arfr = ((ThisApplication) ((AppCompatActivity) appcontext).getApplication()).arFragment;
        if(((ThisApplication)((AppCompatActivity)appcontext).getApplication()).mode == 0)
            return;

        if(arfr!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            }).start();
        }
    }


    Map<String,DetectedObject> detected_objs = new HashMap<>();
    FirebaseVisionImage image = null;

    public void detect(Image img){
        ready = false;
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(img, 1);
        this.image = image;
        img.close();

        // Text Recognizer...
        textRecognizer.processImage(image).addOnSuccessListener(firebaseVisionText -> {
            String text = "";
            for(FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks())
                for (FirebaseVisionText.Line line: block.getLines())
                    text += line.getText();

            Log.e("DETECTED_TEXT : ",text);
            if(text.length()==0);
            else {
                VoiceClass.speak("Detected text :" + text);
                try {
                    Thread.sleep(text.length() * 100);
                }catch (Exception e){ Log.e("Exception","Exception"); }
            }

            //Object Detector starting...
            objectDetector.processImage(image).addOnSuccessListener(detectedObjects -> {

                for (FirebaseVisionObject o : detectedObjects) {
                    Log.e("BBD", o.getBoundingBox().toShortString());
                    Rect extraRect = new Rect(Math.max(0, o.getBoundingBox().left - MARGIN), Math.max(0, o.getBoundingBox().top - MARGIN), Math.min(o.getBoundingBox().right + MARGIN, image.getBitmap().getWidth()), Math.min(o.getBoundingBox().bottom + MARGIN, image.getBitmap().getHeight()));

                    //Labeler starting...
                    labeler.processImage(FirebaseVisionImage.fromBitmap(Bitmap.createBitmap(image.getBitmap(), extraRect.left, extraRect.top, extraRect.width(), extraRect.height()))).addOnSuccessListener(firebaseVisionImageLabels -> {

                        String product = "";
                        for (FirebaseVisionImageLabel l : firebaseVisionImageLabels) {
                            Log.e("LOL", l.getText() + " " + l.getConfidence());
                            if (l.getConfidence() >= 0.7) {
                                DetectedObject object = new DetectedObject(l.getText(), l.getConfidence());
                                object.setX_Y((double) (o.getBoundingBox().left + o.getBoundingBox().right) / 2, (double) (o.getBoundingBox().top + o.getBoundingBox().bottom) / 2);
                                detected_objs.put(product, object);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FAIL1","1");
                    });
                }
                Log.e("DIVIDE","CONQUER");
                if(detected_objs.size()!=0)
                    setDistances();
                ready = true;
            }).addOnFailureListener(e -> {
                Log.e("FAIL2","2");
            });;

        }).addOnFailureListener(e ->{
            Log.e("FAIL3","3");
            //objectDetector.processImage(image).addOnSuccessListener(detectlistener);
        });
        //TODO : ----------------------------------------------------------------------------
    }


    private void setDistances() {
        MotionEvent motionEvent = null;
        Log.e("SETD",String.valueOf(detected_objs.size()));

        for(DetectedObject o : detected_objs.values()){
            motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis()+10, MotionEvent.ACTION_UP,(float) o.getX(),(float)o.getY(),2);
            Collection<HitResult> results =  f.hitTest(motionEvent);
            if(results.size()==0)continue;
            HitResult currentHit = (HitResult) results.toArray()[0];
            o.setDistance(currentHit.getDistance());
            Log.e("SET",String.valueOf(currentHit.getDistance()));
        }

        say();

    }

    private void say() {
        StringBuilder sb = new StringBuilder();
        for(String str: detected_objs.keySet()) {
            sb.append(detected_objs.get(str).getProduct() + " ");
            if(detected_objs.get(str).getDistance()!=0)sb.append(" at "+String.format("%.1f",detected_objs.get(str).getDistance()) +" meters");
        }
        Log.e("OUTCOME",sb.toString());
        voiceClass.speak(sb.toString());
        try {
            Thread.sleep(100 * sb.toString().length());
        }catch (Exception e){ }

        detected_objs.clear();
    }

    public void setVoiceClass(VoiceClass voiceClass) {
        this.voiceClass = voiceClass;
    }


    Frame f=null;
    boolean ready = true;

    @Override
    public void onUpdate(FrameTime frameTime) {
        Log.e("UPDATE","AR_UPDT");
        if(!ready){Log.e("LOL","NOT READY"); return;}

        Session arSession = arfr.getArSceneView().getSession();
        if(arSession==null)return;

        try { f = arSession.update(); }
        catch (CameraNotAvailableException e) { }

        if(f==null)return;

        try {
            Log.e("DETECT M","d");
            detect(f.acquireCameraImage());
        }catch (Exception e){
        }

    }
}