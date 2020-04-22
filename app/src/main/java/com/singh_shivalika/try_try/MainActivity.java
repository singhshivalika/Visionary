package com.singh_shivalika.try_try;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.TextureView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SelfLocation selfLocation;

    private double currentLat,currentLng;
    public static VoiceClass voiceClass;
    ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(()->{ init(); }).start();
    }

    private void init() {
        voiceClass = new VoiceClass(this);
        ((ThisApplication)getApplication()).voiceClass = voiceClass;
        objectDetector = new ObjectDetector(this);
        ((ThisApplication)getApplication()).setObjectDetector(objectDetector);

        runOnUiThread(() -> {
            selfLocation = new SelfLocation(MainActivity.this);
            Log.e("Current Location", "" + selfLocation.LATITUDE + " " + selfLocation.LONGITUDE);
            currentLat = 28.663067;
            currentLng = 77.452757;
        /*currentLat = selfLocation.LATITUDE;
        currentLng = selfLocation.LONGITUDE;*/
            MainActivity.this.askUser();
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
                            Intent i = new Intent(MainActivity.this,GiveDirection.class);
                            i.putExtra("DATAPOINTS",dp.substring(1,dp.length()-1));
                            startActivity(i);
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
            URL url = new URL("https://us1.locationiq.com/v1/search.php?key="+ URLEncoder.encode(getString(R.string.geocode_api))+"&q="+URLEncoder.encode(name).toLowerCase()+"&format=json");
            c =  (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(3000);

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

    public void onObjectDetected(List<String> detectedObjects) {

    }
}