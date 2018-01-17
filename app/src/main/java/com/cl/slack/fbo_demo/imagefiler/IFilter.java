package com.cl.slack.fbo_demo.imagefiler;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cl.slack.fbo_demo.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by slack
 * on 18/1/14 下午6:12
 */

public abstract class IFilter {

    private FloatBuffer mVerBuffer;
    private FloatBuffer mTexBuffer;

    private int mProgram;

    private int glPosition;
    private int glTexture;
    private int glCoordinate;
    private int glMatrix;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private Bitmap mBitmap;
    private int mTextureId;

    private final float[] sPos = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public IFilter() {

        initBuffer();
    }

    private void initBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVerBuffer = bb.asFloatBuffer();
        mVerBuffer.put(sPos);
        mVerBuffer.position(0);
        ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        mTexBuffer = cc.asFloatBuffer();
        mTexBuffer.put(sCoord);
        mTexBuffer.position(0);
    }

    public void onCreate() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        mProgram = ShaderUtils.createProgram(obtainVertex(), obtainFragment());
        glPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        glTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        onExtraCreated(mProgram);
    }

    abstract String obtainVertex();

    abstract String obtainFragment();

    abstract void onExtraCreated(int mProgram);

    public void setTexBuffer(float[] coord) {
        mTexBuffer.clear();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }

    public void onSizeChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float sWidthHeight = width / (float) height;

        float sWH;
        if(mBitmap != null) {
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            sWH = w / (float) h;
        } else {
            sWH = sWidthHeight;
        }

        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 5);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 5);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    public void onDrawFrame() {
        onClear();
        onUseProgram();
        onExtraData();
        onBindTexture();
        onDraw();
    }

    protected void onDraw() {
        GLES20.glEnableVertexAttribArray(glPosition);
        GLES20.glVertexAttribPointer(glPosition,2,GLES20.GL_FLOAT,false,0, mVerBuffer);
        GLES20.glEnableVertexAttribArray(glCoordinate);
        GLES20.glVertexAttribPointer(glCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glDisableVertexAttribArray(glPosition);
        GLES20.glDisableVertexAttribArray(glCoordinate);
    }

    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        GLES20.glUniform1i(glTexture, 0);
    }

    protected void onExtraData() {
        GLES20.glUniformMatrix4fv(glMatrix,1,false,mMVPMatrix,0);
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        if(mBitmap != null) {
            mTextureId = ShaderUtils.createTexture(mBitmap);
        }
    }

    public int getTextureId() {
        return mTextureId;
    }

    public void setTextureId(int mTextureId) {
        this.mTextureId = mTextureId;
    }
}
