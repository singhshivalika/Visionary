package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.camera2.internal.Camera2CameraFactory;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.core.impl.ImageAnalysisConfig;
import androidx.camera.core.impl.LensFacingConverter;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.impl.UseCaseConfig;
import androidx.camera.core.impl.UseCaseConfig.Builder;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraClass {

    PreviewView previewView;
    AppCompatActivity currentActivity;

    CameraClass(PreviewView previewView, AppCompatActivity currentActivity){
        this.currentActivity = currentActivity;
        this.previewView = previewView;
    }

    @SuppressLint("RestrictedApi")
    public void startCamera() {
        CameraXConfig config = CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()).build();
        CameraX.initialize(currentActivity,config);
        stopCamera();
        Preview preview = new Preview.Builder().setTargetResolution(new Size(previewView.getWidth(),previewView.getHeight())).build();
        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        ((ThisApplication)currentActivity.getApplication()).imageAnalysis = imageAnalysis;

        try {

        }catch (Exception e){ }

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
        CameraX.bindToLifecycle((LifecycleOwner)currentActivity, selector, imageAnalysis, preview);
    }

    @SuppressLint("RestrictedApi")
    public void stopCamera(){
        CameraX.unbindAll();
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = previewView.getMeasuredWidth();
        float h = previewView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)previewView.getRotation();

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
        //previewView.setTr
    }







}
