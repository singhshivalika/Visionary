package com.singh_shivalika.try_try;

import android.graphics.Matrix;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

public class CameraClass {

    TextureView textureView;
    AppCompatActivity currentActivity;

    CameraClass(TextureView textureView, AppCompatActivity currentActivity){
        this.currentActivity = currentActivity;
        this.textureView = textureView;
    }

    public void startCamera() {
        stopCamera();
        Preview preview = new Preview(new PreviewConfig.Builder().setTargetResolution(new Size(textureView.getWidth(),textureView.getHeight())).build());

        preview.setOnPreviewOutputUpdateListener(output -> {

            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);

            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(currentActivity.getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        CameraX.bindToLifecycle((LifecycleOwner)currentActivity, preview, imgCap);
    }

    public void stopCamera(){
        CameraX.unbindAll();
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }
        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }







}
