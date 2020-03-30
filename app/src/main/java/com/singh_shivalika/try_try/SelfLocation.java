package com.singh_shivalika.try_try;

        import android.annotation.SuppressLint;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.os.IBinder;


public class SelfLocation extends Service {

    Context appcontext;
    Location loc;
    double LATITUDE;
    double LONGITUDE;
    LocationManager locationManager;

    public SelfLocation(Context context) {
        this.appcontext = context;
        getLocation(appcontext);
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LATITUDE = location.getLatitude();
            LONGITUDE = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @SuppressLint("MissingPermission")
    private void getLocation(Context appcontext) {
        locationManager = (LocationManager) appcontext.getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0,0, listener);

        if (locationManager != null) {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                LATITUDE = loc.getLatitude();
                LONGITUDE = loc.getLongitude();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}