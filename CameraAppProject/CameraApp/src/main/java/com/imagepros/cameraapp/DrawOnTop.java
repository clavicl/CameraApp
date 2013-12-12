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
    Bitmap mBitmap;
    Paint mPaintBlack;
    byte[] mYUVData;
    int[] mRGBData;
    int mImageWidth, mImageHeight;
    double[] mBinSquared;

    List<Bitmap> frames;
    boolean record;
    boolean framesCaptured;

    public DrawOnTop(Context context) {
        super(context);

        frames= new ArrayList<Bitmap>();
        record = false;
        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setTextSize(25);
        framesCaptured = false;

        mBitmap = null;
        mYUVData = null;
        mRGBData = null;
        mBinSquared = new double[256];
        for (int bin = 0; bin < 256; bin++)
        {
            mBinSquared[bin] = ((double)bin) * bin;
        } // bin
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

            // Convert from YUV to RGB
            //decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);
            decodeYUV420SPGrayscale(mRGBData,mYUVData,mImageWidth,mImageHeight);
            // Draw bitmap
            mBitmap.setPixels(mRGBData, 0, mImageWidth, 0, 0,
                    mImageWidth, mImageHeight);
            //save first 100 frames into the list
            if (record && frames.size()< 100)
            {
                frames.add(Bitmap.createScaledBitmap(mBitmap,100,100,false));
            }
            //show toast and set flag to exit activity
            else if(!framesCaptured && frames.size() >=100)
            {
                Toast.makeText(getContext(),"Frame array full",1000).show();
                framesCaptured = true;
            }
            Rect src = new Rect(0, 0,mImageWidth, mImageHeight);
            Rect dst = new Rect(marginWidth, 0,
                    canvasWidth-marginWidth, canvasHeight);
            canvas.drawBitmap(mBitmap, src, dst, mPaintBlack);


            String imageMeanStr = "Mean (R,G,B): ";
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
