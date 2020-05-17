package com.visionary.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class LocationUpdate implements Serializable {

    Point locaton;
    String ownPhoneNo;
    Set<String> starred;

    public LocationUpdate(Point locaton, String ownPhoneNo, Set<String> starred) {
        this.locaton = locaton;
        this.ownPhoneNo = ownPhoneNo;
        this.starred = starred;
    }

    public Point getLocaton() {
        return locaton;
    }

    public void setLocaton(Point locaton) {
        this.locaton = locaton;
    }

    public String getOwnPhoneNo() {
        return ownPhoneNo;
    }

    public void setOwnPhoneNo(String ownPhoneNo) {
        this.ownPhoneNo = ownPhoneNo;
    }

    public Set<String> getStarred() {
        return starred;
    }

    public void setStarred(Set<String> starred) {
        this.starred = starred;
    }
}
