package com.example.kylu.handgeometry;



import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;

import java.util.List;

public class CameraContour extends JavaCameraView {

    private boolean lighOn = false;

    public CameraContour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHightQuality()

    {

        Parameters params = mCamera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        Camera.Size mSize = null;
        for (Camera.Size size : sizes) {
            mSize = size;
        }

        params.setPictureSize(mSize.width, mSize.height);
        mCamera.setParameters(params);

    }

    public void setEffect(String effect) {
        Parameters params = mCamera.getParameters();
        params.setFlashMode(effect);
        mCamera.setParameters(params);
        lighOn = !lighOn;
    }

    public boolean isLighOn() {
        return lighOn;
    }

}
