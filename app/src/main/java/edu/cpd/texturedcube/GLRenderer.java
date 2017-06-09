package edu.cpd.texturedcube;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;
    private static String TAG = "GLRenderer";
    public Cube mCube;
    private float mAngle =0;
    private float mTransY=0;
    private float mTransX=0;
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 40f;


    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private GLES30 mGLES30;

    public GLRenderer(Context context) {


    }



    public void mySetup(GLES30 mGLES30) {
        this.mGLES30 = mGLES30;

    }




    public static int LoadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];


        shader = GLES30.glCreateShader(type);

        if (shader == 0) {
            return 0;
        }


        GLES30.glShaderSource(shader, shaderSrc);


        GLES30.glCompileShader(shader);


        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }




    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {



        GLES30.glClearColor(0.9f, .9f, 0.9f, 0.9f);

        mCube = new Cube();

    }




    public void onDrawFrame(GL10 glUnused) {



        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);


        GLES30.glEnable(GLES30.GL_DEPTH_TEST);


        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);


        Matrix.setIdentityM(mRotationMatrix, 0);


        Matrix.translateM(mRotationMatrix, 0, mTransX, mTransY, 0);


        Matrix.rotateM(mRotationMatrix, 0, mAngle, 1.0f, 1.0f, 1.0f);


        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mRotationMatrix, 0);


        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        mCube.draw(mMVPMatrix);


        mAngle+=.4;

    }




    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES30.glViewport(0, 0, mWidth, mHeight);
        float aspect = (float) width / height;



        Matrix.perspectiveM(mProjectionMatrix, 0, 53.13f, aspect, Z_NEAR, Z_FAR);
    }


    public float getY() {
        return mTransY;
    }

    public void setY(float mY) {
        mTransY = mY;
    }

    public float getX() {
        return mTransX;
    }

    public void setX(float mX) {
        mTransX = mX;
    }

}
