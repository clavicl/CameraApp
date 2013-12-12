package com.imagepros.cameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davides on 12/9/13.
 */
public class DrawOnTop extends View {
    final int TOTAL_FRAMES = 100;
    final int RESIZE_WIDTH = 100;
    final int RESIZE_HEIGHT = 100;

    Bitmap mBitmap;
    Paint mPaintBlack;
    byte[] mYUVData;
    int[] mRGBData;
    int mImageWidth, mImageHeight;

    int[][] TrainingFrames;
    int trainingIndex;
    boolean record;
    boolean framesCaptured;

    public DrawOnTop(Context context) {
        super(context);
        //make space for TOTAL_FRAMES (i.e. 100 frames)
        TrainingFrames = new int[TOTAL_FRAMES][];
        trainingIndex = 0;
        record = false;
        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setTextSize(25);
        framesCaptured = false;

        mBitmap = null;
        mYUVData = null;
        mRGBData = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null)
        {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            int newImageWidth = canvasWidth;
            int newImageHeight = canvasHeight;
            int marginWidth = (canvasWidth - newImageWidth)/2;
            if(trainingIndex < TOTAL_FRAMES && TrainingFrames[trainingIndex] == null)
                TrainingFrames[trainingIndex] =new int[RESIZE_HEIGHT*RESIZE_WIDTH];
            // Convert from YUV to RGB
            //decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);
            decodeYUV420SPGrayscale(mRGBData,mYUVData,mImageWidth,mImageHeight);
            // Draw bitmap
            mBitmap.setPixels(mRGBData, 0, mImageWidth, 0, 0,
                    mImageWidth, mImageHeight);
            //save first 100 frames into the list
            if (record && trainingIndex< TOTAL_FRAMES)
            {
                Bitmap rescaled = Bitmap.createScaledBitmap(mBitmap,100,100,false);
                rescaled.getPixels(TrainingFrames[trainingIndex],0,rescaled.getWidth(),0,0,rescaled.getWidth(),rescaled.getHeight());
                for (int i=0;i<RESIZE_WIDTH*RESIZE_HEIGHT;i++)
                    TrainingFrames[trainingIndex][i] = TrainingFrames[trainingIndex][i] & 0x000000FF;
                trainingIndex++;
            }
            //show toast and set flag to exit activity
            else if(!framesCaptured && trainingIndex >=TOTAL_FRAMES)
            {
                Toast.makeText(getContext(),"Frames captured for training",1000).show();
                framesCaptured = true;
            }


//            Rect src = new Rect(0, 0,m            ImageWidth, mImageHeight);
//            Rect dst = new Rect(marginWidth, 0,
//                    canvasWidth-marginWidth, canvasHeight);
//            canvas.drawBitmap(mBitmap, src, dst, mPaintBlack);


            String imageMeanStr = "Similarity %: ";
            canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintBlack);

        }

        super.onDraw(canvas);

    }

    static public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp, int width, int height)
    {
        final int frameSize = width * height;

        for (int pix = 0; pix < frameSize; pix++)
        {
            int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
            if (pixVal < 0) pixVal = 0;
            if (pixVal > 255) pixVal = 255;
            rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
        }
    }
}
