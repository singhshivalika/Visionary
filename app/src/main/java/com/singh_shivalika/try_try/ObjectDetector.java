package com.singh_shivalika.try_try;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
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

    public void detect() {
        detectedObjects.clear();

        Bitmap bmp =  null;//((ThisApplication) ((MainActivity) appcontext).getApplication()).previewView.
        if(bmp==null)return;

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);
        objectDetector.processImage(image).addOnSuccessListener(detectedObjects -> {

            for(FirebaseVisionObject o : detectedObjects){
                Rect extraRect = new Rect( Math.max(0,o.getBoundingBox().left-MARGIN),Math.max(0,o.getBoundingBox().top-MARGIN),Math.min(o.getBoundingBox().right+MARGIN,bmp.getWidth()),Math.min(o.getBoundingBox().bottom+MARGIN,bmp.getHeight()));
                labeler.processImage(FirebaseVisionImage.fromBitmap(Bitmap.createBitmap( bmp,extraRect.left,extraRect.top,extraRect.width(),extraRect.height()))).addOnSuccessListener(this);
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
        if(detectedObjects.size()==0){detect(); return;}
        StringBuilder sb = new StringBuilder();
        for(String str: detectedObjects)
            sb.append(str+" ");

        voiceClass.speak(sb.toString());
        try {
            Thread.sleep(100 * sb.toString().length());
        }catch (Exception e){ }

        if(((ThisApplication)((AppCompatActivity)appcontext).getApplication()).mode == 1)
            detect();
    }

    public void setVoiceClass(VoiceClass voiceClass) {
        this.voiceClass = voiceClass;
    }
}
