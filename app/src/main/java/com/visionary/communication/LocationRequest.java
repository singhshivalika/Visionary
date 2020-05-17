package com.visionary.communication;

import java.io.Serializable;

public class LocationRequest implements Serializable {

    String tracker;
    String target;

    public LocationRequest(String tracker, String target) {
        this.tracker = tracker;
        this.target = target;
    }

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
