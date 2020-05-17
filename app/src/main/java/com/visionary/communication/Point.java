package com.visionary.communication;

import java.io.Serializable;

public class Point implements Serializable {

    public Point(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }
    double lat,lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
