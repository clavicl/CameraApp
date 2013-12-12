package com.imagepros.cameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by davides on 12/9/13.
 */
public class DrawOnTop extends View {
    final int TOTAL_FRAMES = 200;
    final int MAX_POSITIVE = 100;
    final int RESIZE_WIDTH = 267;//178;
    final int RESIZE_HEIGHT = 150;//100;

    Bitmap mBitmap;
    Bitmap rescaled;
    Paint mPaintRed;
    Paint mPaintGreen;
    Paint mStatusPaint;
    byte[] mYUVData;
    int[] mRGBData;
    int mImageWidth, mImageHeight;
    int mSimilarity;

    int[][] TrainingFrames;
    int trainingIndex;
    int [] A;
    boolean record;
    boolean framesCaptured;
    boolean resetRecord;
    boolean classifyFlag;

    boolean test;
    int testI;

    public DrawOnTop(Context context) {
        super(context);
        //make space for TOTAL_FRAMES (i.e. 100 frames)
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float dp = 20f;
        TrainingFrames = new int[TOTAL_FRAMES][];
        trainingIndex = 0;
        classifyFlag = false;
        record = false;
        resetRecord = false;
        mSimilarity = 0;
        mPaintRed = new Paint();
        mPaintRed.setStyle(Paint.Style.FILL);
        mPaintRed.setColor(Color.RED);
        mPaintRed.setTextSize((int)(metrics.density*dp+.5f));
        mPaintRed.setFakeBoldText(true);
        mPaintGreen = new Paint();
        mPaintGreen.setStyle(Paint.Style.FILL);
        mPaintGreen.setColor(Color.GREEN);
        mPaintGreen.setTextSize((int)(metrics.density*dp+.5f));
        mPaintRed.setFakeBoldText(true);
        framesCaptured = false;
        mStatusPaint = mPaintRed;

        mBitmap = null;
        mYUVData = null;
        mRGBData = null;

        test = false;
        testI = 0;
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

            if (record && trainingIndex< MAX_POSITIVE)
            {
                rescaled = Bitmap.createScaledBitmap(mBitmap,RESIZE_WIDTH,RESIZE_HEIGHT,false);
                rescaled.getPixels(TrainingFrames[trainingIndex],0,rescaled.getWidth(),0,0,rescaled.getWidth(),rescaled.getHeight());
                for (int i=0;i<RESIZE_WIDTH*RESIZE_HEIGHT;i++)
                    TrainingFrames[trainingIndex][i] = TrainingFrames[trainingIndex][i] & 0x000000FF;
                trainingIndex++;
                if (trainingIndex == MAX_POSITIVE)
                    Toast.makeText(getContext(),"Image Captured, move camera around for further training",2000).show();
            }
            else if(record && trainingIndex >= MAX_POSITIVE && trainingIndex < TOTAL_FRAMES){
                if (resetRecord) {
                    trainingIndex = MAX_POSITIVE;
                    resetRecord = false;
                }
                rescaled = Bitmap.createScaledBitmap(mBitmap,RESIZE_WIDTH,RESIZE_HEIGHT,false);
                rescaled.getPixels(TrainingFrames[trainingIndex],0,rescaled.getWidth(),0,0,rescaled.getWidth(),rescaled.getHeight());
                for (int i=0;i<RESIZE_WIDTH*RESIZE_HEIGHT;i++)
                    TrainingFrames[trainingIndex][i] = TrainingFrames[trainingIndex][i] & 0x000000FF;
                trainingIndex++;
            }
            //show toast and set flag to exit activity
            else if(!framesCaptured && trainingIndex >=TOTAL_FRAMES)
            {
                Toast.makeText(getContext(),"Frames captured for training",1000).show();
                int [] tmp = new int[RESIZE_HEIGHT * RESIZE_WIDTH];
                for (int i=0;i<RESIZE_WIDTH*RESIZE_HEIGHT;i++)
                    tmp[i] = TrainingFrames[1][i] | (TrainingFrames[1][i] << 8) | (TrainingFrames[1][i] << 16);
                rescaled.setPixels(tmp,0,RESIZE_WIDTH, 0, 0,
                        RESIZE_WIDTH,RESIZE_HEIGHT);
                framesCaptured = true;
                classifyFlag = true;
//                A = trainClassifier();
//                Toast.makeText(getContext(),"A matrix trained",1000).show();
            }

            if(test){
                if(testI++ >= 10){
                    testI = 0;
                    int [] tmp= new int[RESIZE_HEIGHT*RESIZE_WIDTH];

                    Bitmap testImage = Bitmap.createScaledBitmap(mBitmap,RESIZE_WIDTH,RESIZE_HEIGHT,false);
                    testImage.getPixels(tmp,0,testImage.getWidth(),0,0,testImage.getWidth(),testImage.getHeight());
                    for (int i=0;i<RESIZE_WIDTH*RESIZE_HEIGHT;i++)
                        tmp[i] = tmp[i] & 0x000000FF;
                    if (classify(A,tmp) > 0) {
                        mStatusPaint = mPaintGreen;
                    }
                    else {
                        mStatusPaint = mPaintRed;
                    }
                }
            }
            if (framesCaptured) {
               canvas.drawBitmap(rescaled, canvasWidth-RESIZE_WIDTH, 0, mPaintGreen);
               canvas.drawCircle(30,30,25,mStatusPaint);
            }

        }

        super.onDraw(canvas);

        if (classifyFlag) {
            classifyFlag = false;
//            new Thread(new Runnable(){
//                   @Override
//                    public void run() {
//                    try{
//                        A = trainClassifier();
//                        Toast.makeText(getContext(),"A matrix trained",1000).show();
//                    }catch (Exception e){
//                        Toast.makeText(getContext(),"something happened",1000).show();
//                    }
//                   }
//                }).start();
            A = trainClassifier();
            Toast.makeText(getContext(),"A matrix trained",1000).show();
        }
    }

   private int [] trainClassifier(){
       Random rand = new Random();
       int D=RESIZE_WIDTH*RESIZE_HEIGHT;
       int [] A = new int[D];
       int [] Aold = new int[D];
       //Initialize A
       for (int i=0;i<D;i++)
           A[i]= 0;
           //A[i]=rand.nextInt(255);
       for (int i=0;i<200;i++){
           boolean flag = false;
           Aold = A;
           for(int j=1;j<TOTAL_FRAMES;j++){
               int c = classify(A,TrainingFrames[j]);
                if (c<= 0 && j < MAX_POSITIVE){
                    flag = true;
                    A = sum(A,TrainingFrames[j]);
                }
               else if (c>0 && j >= MAX_POSITIVE){
                    flag = true;
                    A = diff(A,TrainingFrames[j]);
                }
           }
           if (!flag || norm(diff(Aold,A)) <= 1)
               break;
       }
       return A;
    }
    private double norm(int []A){
        double n = 0;
        for(int i=0;i < A.length;i++)
            n = n+ Math.pow(A[i],2);
        return Math.sqrt(n);
    }
    private int [] diff(int[]A, int[]X){
        int[] s = new int[A.length];
        for(int i=0;i<A.length;i++)
            s[i] = A[i]-X[i];
        return s;
    }
    private int [] sum(int[]A, int[]X){
        int[] s = new int[A.length];
        for(int i=0;i<A.length;i++)
            s[i] = A[i] +X[i];
        return s;
    }
    private int classify(int []A,int[] X){
        int c = 0;
        for(int i=0;i<A.length;i++){
            c = c + (A[i] * X[i]);
        }
        return c;
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
