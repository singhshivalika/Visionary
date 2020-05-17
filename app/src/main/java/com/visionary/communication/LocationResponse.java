package com.visionary.communication;

import java.io.Serializable;

public class LocationResponse implements Serializable {

    String target;
    Point point;

    public LocationResponse(String target, Point point) {
        this.target = target;
        this.point = point;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}

