package com.singh_shivalika.try_try;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    Navigator navigator;
    ArFragment arFragment;

    private SelfLocation selfLocation;
    ConstraintLayout taparea;
    TextView box;
    SwipeListener sl;

    private double currentLat,currentLng;
    public static VoiceClass voiceClass;
    FirebaseVisionObjectDetector objectDetector;
    FirebaseVisionTextRecognizer textRecognizer;
    FirebaseVisionImageLabeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragment);
        arFragment.getView().setVisibility(View.INVISIBLE);
        ((ThisApplication)getApplication()).arFragment = arFragment;
        arFragment.getArSceneView().pauseAsync(AsyncTask.THREAD_POOL_EXECUTOR);

        taparea = findViewById(R.id.tap_area);
        box = findViewById(R.id.box);

        sl = new SwipeListener(MainActivity.this){
            @Override
            public void onSwipeRight() {
                detector();
            }
            @Override
            public void onSwipeLeft() {
                recognizer();
            }
            @Override
            public void onSwipeTop() {
                sos();
            }
            @Override
            public void onSwipeBottom() {
                navigator();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }).start();

    }

    public void init(){
        voiceClass = new VoiceClass(this);
        ((ThisApplication)getApplication()).voiceClass = voiceClass;

        FirebaseVisionObjectDetectorOptions options = new FirebaseVisionObjectDetectorOptions.Builder().setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE).enableClassification().build();
        FirebaseApp.initializeApp(this);
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        runOnUiThread(() -> {
            selfLocation = new SelfLocation(MainActivity.this);
            Log.e("Current Location", "" + selfLocation.LATITUDE + " " + selfLocation.LONGITUDE);
            currentLat = 28.663067;
            currentLng = 77.452757;
            /*currentLat = selfLocation.LATITUDE;
            currentLng = selfLocation.LONGITUDE;*/

            taparea.setOnTouchListener(sl);
        });

    }

    public void askUser(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                voiceClass.speak("Where do you wanna go ? ");
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){ Log.e("ERROR","ERROR");}
                voiceClass.promptSpeechInput();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LOL",""+requestCode+" "+requestCode+" ");

        if(requestCode==101){
            if(data!=null){
                final String dest = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                new Thread(() -> {
                    final LatLng latLng = getLocation(dest, getString(R.string.access_token));
                    if(latLng==null) {
                        voiceClass.speak("Cannot find destination");
                        return;
                    }
                    manualNavigation(latLng);
                }).start();
            }
        }
    }

    private void manualNavigation(LatLng latLng) {
        LatLng currentposition = new LatLng(currentLat,currentLng);
        LatLng destinationposition = latLng;

        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader=null;
                HttpURLConnection c=null;

                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("https://api.mapbox.com/directions/v5/mapbox/walking/"+currentposition.getLongitude()+","+currentposition.getLatitude()+";"+destinationposition.getLongitude()+","+destinationposition.getLatitude()+"?geometries=geojson&access_token="+getString(R.string.access_token));
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                    JSONObject obj = new JSONObject(response.toString());
                    JSONArray arr = new JSONArray(obj.getString("routes"));
                    if(arr.length()>=1)
                        obj = new JSONObject(arr.get(0).toString());

                    String dp = (new JSONObject(obj.getString("geometry"))).getString("coordinates");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startCustomNav(dp.substring(1,dp.length()-1));
                        }
                    });

                }
                catch(Exception e) {
                    Log.e("ERROR", String.valueOf(e.toString()) );
                }
                finally {
                    try {
                        if(reader !=null){
                            reader.close();}
                        c.disconnect();
                    }
                    catch (Exception e){ }
                }
            }
        }).start();
    }

    private LatLng getLocation(final String name, final String token){
        BufferedReader reader=null;
        HttpURLConnection c=null;

        final StringBuilder response= new StringBuilder();
        try {
            //code to connect to server
            URL url = new URL("https://us1.locationiq.com/v1/search.php?key="+ URLEncoder.encode(getString(R.string.geocode_api))+"&q="+URLEncoder.encode(name).toLowerCase()+"&format=json");
            c =  (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(3000);
            //e

            c.setDoOutput(true);
            c.setDoInput(true);

            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

            String line;
            while ((line = reader.readLine()) !=null)
                response.append(line);

            JSONArray arr = new JSONArray(response.toString());

            for(int i = 0; i < arr.length(); i++){
                JSONObject obj = new JSONObject(arr.getString(i));
                if(currentLat-1<obj.getDouble("lat")&&currentLat+1>obj.getDouble("lat") && currentLng-1<obj.getDouble("lon") && currentLng+1> obj.getDouble("lon"))
                    return new LatLng(obj.getDouble("lat"),obj.getDouble("lon"));
                return null;
            }

            return null;
        }
        catch(Exception e) {
            Log.e("ERROR", String.valueOf(e.toString()) );
            return null;
        }
        finally {
            try {
                if(reader !=null){
                    reader.close();}
                c.disconnect();
            }
            catch (Exception e){ return null;}
        }
    }

    //SwipeDown
    private void navigator(){
        ((ThisApplication)getApplication()).mode=ThisApplication.MODE.NAVIGATOR;
        MainActivity.this.askUser();
    }

    //SwipeRight
    private void detector(){
        ((ThisApplication)getApplication()).mode=ThisApplication.MODE.DETECTOR;
        new Thread(new Runnable() {
            @Override
            public void run() {
                arFragment.getArSceneView().getScene().addOnUpdateListener(MainActivity.this);
            }
        }).start();


    }

    //SwipeLeft
    private void recognizer(){
        ((ThisApplication)getApplication()).mode=ThisApplication.MODE.RECOGNIZER;

    }


    //SwipeUp
    private void sos(){
        ((ThisApplication)getApplication()).mode=ThisApplication.MODE.SOS;

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

    Frame f=null;
    boolean ready = true;

    @Override
    public void onUpdate(FrameTime frameTime) {
        if( ((ThisApplication)getApplication()).mode == ThisApplication.MODE.RECOGNIZER || ((ThisApplication)getApplication()).mode == ThisApplication.MODE.DETECTOR);
        else {
            arFragment.getArSceneView().getScene().removeOnUpdateListener(this);
            return;
        }

        Log.e("UPDATE","AR_UPDT");
        if(!ready){Log.e("LOL","NOT READY"); return;}

        Session arSession = arFragment.getArSceneView().getSession();
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



    Map<String,DetectedObject> detected_objs = new HashMap<>();
    FirebaseVisionImage image = null;
    private static final int MARGIN = 10;

    public void detect(Image img){
        ready = false;
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(img, 0);
        this.image = image;
        img.close();

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
        });
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

}