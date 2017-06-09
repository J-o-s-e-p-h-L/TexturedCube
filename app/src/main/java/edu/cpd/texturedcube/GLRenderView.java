package edu.cpd.texturedcube;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.opengl.GLUtils;
import android.view.MotionEvent;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class GLRenderView extends TextureView implements TextureView.SurfaceTextureListener {

    public GLRenderer mRenderer;
    private RenderThread thread;
    public boolean isRunning = false;
    private boolean mPaused = true;
    private boolean rendererChanged = false;
    private static final int TARGET_FPS = 55;
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private SurfaceTexture mSurface;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private EGL10 mEgl;
    private EGLConfig eglConfig;
    private GL10  mGl;
    private int targetFrameMillis;
    private int renderHeight;
    private int renderWidth;
    private int targetFps;


    public GLRenderView(Context context) {
        super(context);
        initialize(context);
    }

    public GLRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public GLRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }



    public synchronized void setRenderer(GLRenderer renderer){
        mRenderer  = renderer;
        rendererChanged = true;
    }


    private void initialize(Context context) {
        targetFps = TARGET_FPS;

        setSurfaceTextureListener(this);
    }

    private static final float TOUCH_SCALE_FACTOR = 0.0075f;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {




        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                if (x > 0) {
                    float dx = x - mPreviousX;


                    mRenderer.setX(mRenderer.getX() - (dx * TOUCH_SCALE_FACTOR));
                }
                if (y > 0) {
                    float dy = y - mPreviousY;
                    mRenderer.setY(mRenderer.getY() - (dy * TOUCH_SCALE_FACTOR));
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startThread(surface, width, height, targetFps);
    }

    public void startThread(SurfaceTexture surface, int width, int height, float targetFramesPerSecond){
        thread = new RenderThread();
        mSurface = surface;
        setDimensions(width, height);
        targetFrameMillis = (int) ((1f/targetFramesPerSecond)*1000);

        thread.start();

    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        setDimensions(width, height);
        if(mRenderer != null)
            mRenderer.onSurfaceChanged(mGl, width, height);
    }
    public synchronized void setPaused(boolean isPaused){
        mPaused = isPaused;
    }

    public synchronized boolean isPaused(){
        return mPaused;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopThread();
        return false;
    }

    public void stopThread(){
        if(thread != null){
            isRunning = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            thread = null;
        }

    }

    private boolean shouldSleep(){
        return isPaused() || mRenderer == null;
    }

    private class RenderThread extends Thread {
        @Override
        public void run() {
            isRunning = true;

            initGL();
            checkGlError();

            long lastFrameTime = System.currentTimeMillis();

            while (isRunning) {
                while (mRenderer == null){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e){

                    }

                }

                if(rendererChanged){

                    initializeRenderer(mRenderer);
                    rendererChanged = false;
                }

                if (!shouldSleep()) {

                    lastFrameTime = System.currentTimeMillis();

                    drawSingleFrame();
                }

                try {
                    if (shouldSleep())
                        Thread.sleep(100);
                    else {
                        long thisFrameTime = System.currentTimeMillis();
                        long timDiff = thisFrameTime - lastFrameTime;
                        lastFrameTime = thisFrameTime;
                        Thread.sleep(Math.max(10l, targetFrameMillis - timDiff));
                    }
                } catch (InterruptedException e) {

                }
            }
        }

    }
    private synchronized void initializeRenderer(GLRenderer renderer) {
        if(renderer != null && isRunning) {
            renderer.onSurfaceCreated(mGl, eglConfig);
            renderer.onSurfaceChanged(mGl, renderWidth, renderHeight);
        }
    }

    private synchronized void drawSingleFrame() {
        checkCurrent();

        if(mRenderer != null) {
            mRenderer.onDrawFrame(mGl);
        }
        checkGlError();
        if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
        }
    }

    public void setDimensions(int width, int height){
        renderWidth = width;
        renderHeight = height;
    }

    private void checkCurrent() {
        if (!mEglContext.equals(mEgl.eglGetCurrentContext())
                || !mEglSurface.equals(mEgl
                .eglGetCurrentSurface(EGL10.EGL_DRAW))) {
            checkEglError();
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                    mEglSurface, mEglContext)) {
                throw new RuntimeException(
                        "eglMakeCurrent error "
                                + GLUtils.getEGLErrorString(mEgl
                                .eglGetError()));
            }
            checkEglError();
        }
    }

    private void checkEglError() {
        final int error = mEgl.eglGetError();
        if (error != EGL10.EGL_SUCCESS) {
        }
    }


    private void checkGlError() {
        final int error = mGl.glGetError();
        if (error != GL11.GL_NO_ERROR) {
        }
    }

    private void initGL() {

        mEgl = (EGL10) EGLContext.getEGL();
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay error "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize error "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };
        eglConfig = null;
        if (!mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1,
                configsCount)) {
            throw new IllegalArgumentException(
                    "eglChooseConfig error "
                            + GLUtils.getEGLErrorString(mEgl
                            .eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialised");
        }
        int[] attrib_list = {
                EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay,
                eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError();
        mEglSurface = mEgl.eglCreateWindowSurface(
                mEglDisplay, eglConfig, mSurface, null);
        checkEglError();
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                return;
            }
            throw new RuntimeException(
                    "eglCreateWindowSurface error "
                            + GLUtils.getEGLErrorString(error));
        }
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent error "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        checkEglError();
        mGl = (GL10) mEglContext.getGL();
        checkEglError();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

}
