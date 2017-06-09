package edu.cpd.texturedcube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Color;
import android.opengl.GLES30;
import android.util.Log;

public class Cube {
    private int mProgramObject;
    private int mMVPMatrixHandle;
    private int mColorHandle;
    private FloatBuffer mVertices;
    private float s = 0.33f;
    private float[] mVerticesData = new float[]{

            -s, s, s,
            -s, -s, s,
            s, -s, s,
            s, -s, s,
            s, s, s,
            -s, s, s,

            -s, s, -s,
            -s, -s, -s,
            s, -s, -s,
            s, -s, -s,
            s, s, -s,
            -s, s, -s,

            -s, s, -s,
            -s, -s, -s,
            -s, -s, s,

            -s, -s, s,
            -s, s, s,
            -s, s, -s,


            s, s, -s,
            s, -s, -s,
            s, -s, s,

            s, -s, s,
            s, s, s,
            s, s, -s,


            -s, s, -s,
            -s, s, s,
            s, s, s,

            s, s, s,
            s, s, -s,
            -s, s, -s,


            -s, -s, -s,
            -s, -s, s,
            s, -s, s,

            s, -s, s,
            s, -s, -s,
            -s, -s, -s
    };

    private float[] colour1() {
        return new float[]{
                Color.red(Color.YELLOW) / 255f,
                Color.green(Color.YELLOW) / 255f,
                Color.blue(Color.YELLOW) / 255f,
                1.0f
        };
    }

    static float[] colour2() {
        return new float[]{
                Color.red(Color.GREEN) / 255f,
                Color.green(Color.GREEN) / 255f,
                Color.blue(Color.GREEN) / 255f,
                1.0f
        };
    }

    static float[] colour3() {
        return new float[]{
                Color.red(Color.CYAN) / 255f,
                Color.green(Color.CYAN) / 255f,
                Color.blue(Color.CYAN) / 255f,
                1.0f
        };
    }

    static float[] colour4() {
        return new float[]{
                Color.red(Color.RED) / 255f,
                Color.green(Color.RED) / 255f,
                Color.blue(Color.RED) / 255f,
                1.0f
        };
    }

    static float[] colour5() {
        return new float[]{
                Color.red(Color.MAGENTA) / 255f,
                Color.green(Color.MAGENTA) / 255f,
                Color.blue(Color.MAGENTA) / 255f,
                1.0f
        };
    }

    static float[] colour6() {
        return new float[]{
                Color.red(Color.BLUE) / 255f,
                Color.green(Color.BLUE) / 255f,
                Color.blue(Color.BLUE) / 255f,
                1.0f
        };
    }

    float c1[] = colour1();
    float c2[] = colour2();
    float c3[] = colour3();
    float c4[] = colour4();
    float c5[] = colour5();
    float c6[] = colour6();


    String vShaderStr =
            "#version 300 es 			  \n"
                    + "uniform mat4 uMVPMatrix;     \n"
                    + "in vec4 vPosition;           \n"
                    + "void main()                  \n"
                    + "{                            \n"
                    + "   gl_Position = uMVPMatrix * vPosition;  \n"
                    + "}                            \n";

    String fShaderStr =
            "#version 300 es		 			          	\n"
                    + "precision mediump float;					  	\n"
                    + "uniform vec4 vColor;	 			 		  	\n"
                    + "out vec4 fragColor;	 			 		  	\n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  fragColor = vColor;                    	\n"
                    + "}                                            \n";

    String TAG = "Cube";

    public Cube() {
        mVertices = ByteBuffer
                .allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVerticesData);
        mVertices.position(0);

        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        vertexShader = GLRenderer.LoadShader(GLES30.GL_VERTEX_SHADER, vShaderStr);
        fragmentShader = GLRenderer.LoadShader(GLES30.GL_FRAGMENT_SHADER, fShaderStr);
        programObject = GLES30.glCreateProgram();

        if (programObject == 0) {
            Log.e(TAG, "So some kind of error, but what?");
            return;
        }

        GLES30.glAttachShader(programObject, vertexShader);
        GLES30.glAttachShader(programObject, fragmentShader);
        GLES30.glBindAttribLocation(programObject, 0, "vPosition");
        GLES30.glLinkProgram(programObject);
        GLES30.glGetProgramiv(programObject, GLES30.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program:");
            Log.e(TAG, GLES30.glGetProgramInfoLog(programObject));
            GLES30.glDeleteProgram(programObject);
            return;
        }

        mProgramObject = programObject;
    }

    public void draw(float[] mvpMatrix) {

        GLES30.glUseProgram(mProgramObject);

        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramObject, "uMVPMatrix");
        GLRenderer.checkGlError("glGetUniformLocation");


        mColorHandle = GLES30.glGetUniformLocation(mProgramObject, "vColor");

        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLRenderer.checkGlError("glUniformMatrix4fv");

        int VERTEX_POS_INDX = 0;
        mVertices.position(VERTEX_POS_INDX);


        GLES30.glVertexAttribPointer(VERTEX_POS_INDX, 3, GLES30.GL_FLOAT,
                false, 0, mVertices);
        GLES30.glEnableVertexAttribArray(VERTEX_POS_INDX);


        int startPos = 0;
        int verticesPerface = 6;


        GLES30.glUniform4fv(mColorHandle, 1, c1, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        GLES30.glUniform4fv(mColorHandle, 1, c2, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        GLES30.glUniform4fv(mColorHandle, 1, c3, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        GLES30.glUniform4fv(mColorHandle, 1, c4, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        GLES30.glUniform4fv(mColorHandle, 1, c5, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        GLES30.glUniform4fv(mColorHandle, 1, c6, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);


    }
}
