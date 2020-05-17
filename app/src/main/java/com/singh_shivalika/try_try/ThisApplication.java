package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;

import androidx.camera.core.ImageAnalysis;


import com.google.ar.sceneform.ux.ArFragment;
import com.visionary.communication.Establishment;
import com.visionary.communication.LocationUpdate;
import com.visionary.communication.Point;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ThisApplication extends Application {

    Socket socket;
    ObjectOutputStream writer;
    ObjectInputStream reader;

    public void shareLocation() {
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") LocationUpdate update = new LocationUpdate(new Point(selfLocation.LATITUDE,selfLocation.LONGITUDE),mngr.getLine1Number(),SOS.starredContacts);
        try {
            writer.reset();
            writer.writeUnshared(update);
        }catch (Exception e) { }
    }

    public static class MODE{

        public static final int NO_OPERATION = 0;
        public static final int NAVIGATOR = 1;
        public static final int DETECTOR = 2;
        public static final int RECOGNIZER = 3;
        public static final int SOS = 4;
    }

    public ArFragment arFragment;
    private boolean give_Instruction = false;
    public VoiceClass voiceClass;
    public SelfLocation selfLocation;
    public ObjectDetector objectDetector;
    ImageAnalysis imageAnalysis;
    public CameraClass cameraClass;

    public int mode = 0;

    public void setImageAnalysis(ImageAnalysis imageAnalysis){
        this.imageAnalysis = imageAnalysis;
    }

    public void setObjectDetector(ObjectDetector objectDetector){
        this.objectDetector = objectDetector;
        objectDetector.setVoiceClass(voiceClass);
    }

    public void setGive_Instruction(boolean give_Instruction) {
        this.give_Instruction = give_Instruction;
    }

    public boolean isGive_Instruction() {
        return give_Instruction;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        give_Instruction = false;
    }

    public void setCameraClass(CameraClass cameraClass) {
        this.cameraClass = cameraClass;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToServer();
            }
        }).start();

    }

    private void connectToServer() {
        try {
            socket = new Socket("192.168.1.4", 2180);
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());

            Establishment e = new Establishment(Establishment.Type.BLIND);
            writer.reset();
            writer.writeUnshared(e);
        }catch (Exception e){}
    }


}
