package edu.cpd.texturedcube;

import android.support.v7.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    GLRenderView mGLTextureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (detectOpenGLES30()) {
            mGLTextureView = new GLRenderView(this);
            mGLTextureView.setRenderer(new GLRenderer(this));
            setContentView(mGLTextureView);
        } else {
            finish();
        }

    }

    private boolean detectOpenGLES30() {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mGLTextureView.setPaused(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGLTextureView.isRunning == false) {
            mGLTextureView = new GLRenderView(this);
            mGLTextureView.setRenderer(new GLRenderer(this));
            setContentView(mGLTextureView);
        }
        mGLTextureView.setPaused(false);

    }
}
