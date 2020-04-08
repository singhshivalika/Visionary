package com.singh_shivalika.try_try;

import android.app.Application;
import android.view.TextureView;

public class ThisApplication extends Application {

    private boolean give_Instruction = false;
    public VoiceClass voiceClass;
    public ObjectDetector objectDetector;
    TextureView textureView;
    public CameraClass cameraClass;

    public int mode = 0;
    //0 navigation
    //1 object

    public void setTextureView(TextureView textureView){
        this.textureView = textureView;
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
