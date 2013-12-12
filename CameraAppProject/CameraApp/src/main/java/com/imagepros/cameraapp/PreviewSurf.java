package com.imagepros.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by davides on 12/9/13.
 */
public class PreviewSurf  extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder mHolder;

    Activity mContext;
    Camera mCamera;
    DrawOnTop mDrawOnTop;
    boolean mFinished;

    PreviewSurf(Context context, DrawOnTop drawOnTop) {
        super(context);

        mContext = (Activity)context;
        mDrawOnTop = drawOnTop;
        mFinished = false;
        mDrawOnTop.record = false;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewDisplay(holder);
            // Preview callback used whenever new viewfinder frame is available
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera)
                {
                    if ( (mDrawOnTop == null) || mFinished )
                        return;

                    if (mDrawOnTop.mBitmap == null)
                    {
                        // Initialize the draw-on-top companion
                        Camera.Parameters params = camera.getParameters();
//                        swapped the width and height
                        mDrawOnTop.mImageWidth = params.getPreviewSize().width;
                        mDrawOnTop.mImageHeight = params.getPreviewSize().height;
                        mDrawOnTop.mBitmap = Bitmap.createBitmap(mDrawOnTop.mImageWidth,
                                mDrawOnTop.mImageHeight, Bitmap.Config.RGB_565);

                        mDrawOnTop.mRGBData = new int[mDrawOnTop.mImageWidth * mDrawOnTop.mImageHeight];
                        mDrawOnTop.mYUVData = new byte[data.length];
                    }

                    // Pass YUV data to draw-on-top companion
                    System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0, data.length);
                    mDrawOnTop.invalidate();
                }
            });
        }
        catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mFinished = true;
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(320, 240);
//        parameters.setPreviewFrameRate(15);
//        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        mCamera.startPreview();

    }
}
