package com.singh_shivalika.try_try;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.location.LocationEngine;
//import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapboxMap map;
    private Button startButton;
    private SelfLocation selfLocation;
    PermissionsManager permissionsManager;
    private Marker originMarker;
    private Marker destinationMarker;
    DirectionsRoute current_route;

    EditText searchbox;

    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MapActivity";

    private double currentLat,currentLng;
    public static VoiceClass voiceClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        searchbox = findViewById(R.id.search_box);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        voiceClass = new VoiceClass(this);
        mapView.getMapAsync(this);
    }

    public void askUser(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                voiceClass.speak("Abbe andhe, kahan jana chahta hain ?");
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final LatLng latLng = getLocation(dest, getString(R.string.access_token));
                        if(latLng==null) {
                            voiceClass.speak("Cannot find destination");
                            return;
                        }
                        manualNavigation(latLng);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handle(latLng);
                            }
                        });
                    }
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

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> { });

        selfLocation = new SelfLocation(this);
        Log.e("Current Locaton",""+selfLocation.LATITUDE+" "+selfLocation.LONGITUDE);
        currentLat = 28.663067;
        currentLng  = 77.452757;
        /*currentLat = selfLocation.LATITUDE;
        currentLng = selfLocation.LONGITUDE;*/

        originMarker = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(currentLat,currentLng)));

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(selfLocation.LATITUDE,selfLocation.LONGITUDE))
                        .zoom(mapboxMap.getCameraPosition().zoom)
                        .build());
            }
        });
        this.map = mapboxMap;

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

    private void handle(LatLng point) {
        Log.e(String.valueOf(point.getLatitude()),String.valueOf(point.getLongitude()));
        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        Point originPoint = Point.fromLngLat(originMarker.getPosition().getLongitude(), originMarker.getPosition().getLatitude());
        Point destinationPoint = Point.fromLngLat(destinationMarker.getPosition().getLongitude(), destinationMarker.getPosition().getLatitude());
        getRoute(originPoint, destinationPoint);
    }

    private void getRoute(Point origin, Point destination) {
        Log.e("RESP","RESP");
        NavigationRoute.builder(this)
                .accessToken(getString(R.string.access_token))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.e("RESP","RESP");
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().routes().isEmpty()) {
                            List<DirectionsRoute> routes = response.body().routes();
                            if(routes.size()>=1)current_route = routes.get(0);
                            Log.e("1", String.valueOf(routes.size()));
                            navigationMapRoute = new NavigationMapRoute(null,mapView,map);
                            navigationMapRoute.addRoutes(routes);

                            /*if(current_route!=null)
                                startnavigating(current_route);*/
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e("RESP","RESP");
                        Toast.makeText(MainActivity.this,"Cannot Find path",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startnavigating(DirectionsRoute current_route) {
        NavigationLauncherOptions opts = NavigationLauncherOptions.builder()
                .directionsRoute(current_route)
                .shouldSimulateRoute(false)
                .build();

        NavigationLauncher.startNavigation(MainActivity.this, opts);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (navigationMapRoute != null) {
            navigationMapRoute.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (navigationMapRoute != null) {
            navigationMapRoute.onStop();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}