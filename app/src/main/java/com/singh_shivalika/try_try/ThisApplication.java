package com.singh_shivalika.try_try;

import android.app.Application;
import android.view.TextureView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.impl.ImageAnalysisConfig;
import androidx.camera.view.PreviewView;

import com.google.ar.sceneform.ux.ArFragment;

public class ThisApplication extends Application {

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
}
