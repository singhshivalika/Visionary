package com.singh_shivalika.try_try;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.label.ImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;

public class DetectedObject {

    private double distance,X,Y;
    private String product;
    private double confidence;

    DetectedObject(){}

    DetectedObject(String product, double confidence){
        this.product = product;
        this.confidence = confidence;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
