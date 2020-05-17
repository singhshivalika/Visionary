package com.visionary.communication;

import java.io.Serializable;
import java.util.ArrayList;

public class LocationUpdate implements Serializable {

    Point locaton;
    String ownPhoneNo;
    ArrayList<String> starred;

    public LocationUpdate(Point locaton, String ownPhoneNo, ArrayList<String> starred) {
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

    public ArrayList<String> getStarred() {
        return starred;
    }

    public void setStarred(ArrayList<String> starred) {
        this.starred = starred;
    }
}
