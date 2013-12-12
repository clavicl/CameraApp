package com.imagepros.cameraapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {
    private PreviewSurf mPreview;
    private DrawOnTop mDrawOnTop;
    private Button recordButton;
    private Button testButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDrawOnTop = new DrawOnTop(this);
        mPreview = new PreviewSurf(this, mDrawOnTop);

        setContentView(R.layout.activity_camera);
        recordButton = (Button) findViewById(R.id.record_button);
        testButton = (Button)findViewById(R.id.test_button);

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    mDrawOnTop.record = true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    mDrawOnTop.record = false;
                    mDrawOnTop.resetRecord = true;
                }
                return true;
            }
        });

        testButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    mDrawOnTop.test = true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    mDrawOnTop.test = false;
                }
                //do something here
                return true;
            }
        });
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        FrameLayout DrawOnTopPreview = (FrameLayout) findViewById(R.id.drawOnTop_preview);
        DrawOnTopPreview.addView(mDrawOnTop);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
