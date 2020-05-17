package com.visionary.communication;

import java.io.Serializable;

public class Establishment implements Serializable {

    public static class Type{
        public static final int BLIND = 1;
        public static final int TRACKER = 2;
    }

    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Establishment(int type) {
        this.type = type;
    }
}
