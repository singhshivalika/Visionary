package com.singh_shivalika.try_try;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.label.ImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;

public class DetectedObject extends FirebaseVisionImageLabel {

    private double distance,X,Y;

    public DetectedObject(@NonNull ImageLabel imageLabel) {
        super(imageLabel);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public void setX_Y(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }
}
