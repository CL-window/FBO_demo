package com.cl.slack.fbo_demo.imagefiler;

import android.opengl.GLES20;

/**
 * Created by slack
 * on 18/1/14 下午6:12
 */

public class ColorFiler extends IFilter {

    private int vChangeType;
    private int vChangeColor;
    private Filter mFilter;

    public ColorFiler(Filter mFilter) {
        this.mFilter = mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    @Override
    String obtainVertex() {
        return "attribute vec4 vPosition;\n" +
                "attribute vec2 vCoordinate;\n" +
                "uniform mat4 vMatrix;\n" +
                "varying vec2 aCoordinate;\n" +
                "void main(){\n" +
                "    gl_Position=vMatrix*vPosition;\n" +
                "    aCoordinate=vCoordinate;\n" +
                "}";
    }

    @Override
    String obtainFragment() {
        return "precision mediump float;\n" +
                "uniform sampler2D vTexture;\n" +
                "uniform int vChangeType;\n" +
                "uniform vec3 vChangeColor;\n" +
                "varying vec2 aCoordinate;\n" +
                "void modifyColor(vec4 color){\n" +
                "    color.r=max(min(color.r,1.0),0.0);\n" +
                "    color.g=max(min(color.g,1.0),0.0);\n" +
                "    color.b=max(min(color.b,1.0),0.0);\n" +
                "    color.a=max(min(color.a,1.0),0.0);\n" +
                "}\n" +
                "void main(){\n" +
                "    vec4 nColor=texture2D(vTexture,aCoordinate);\n" +
                "   if(vChangeType==1){\n" +
                "        float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;\n" +
                "        gl_FragColor=vec4(c,c,c,nColor.a);\n" +
                "    }else if(vChangeType==2){\n" +
                "        vec4 deltaColor=nColor+vec4(vChangeColor,0.0);\n" +
                "        modifyColor(deltaColor);\n" +
                "        gl_FragColor=deltaColor;\n" +
                "    }else{\n" +
                "        gl_FragColor=nColor;\n" +
                "    }\n" +
                "}";
    }

    @Override
    void onExtraCreated(int mProgram) {
        vChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        vChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
    }

    @Override
    protected void onExtraData() {
        super.onExtraData();
        GLES20.glUniform1i(vChangeType, mFilter.getType());
        GLES20.glUniform3fv(vChangeColor, 1, mFilter.data(), 0);
    }

    public enum Filter {

        NONE(0, new float[]{0.0f, 0.0f, 0.0f}),
        GRAY(1, new float[]{0.299f, 0.587f, 0.114f}),
        COOL(2, new float[]{0.0f, 0.0f, 0.1f}),
        WARM(2, new float[]{0.1f, 0.1f, 0.0f}),
        BLUR(3, new float[]{0.006f, 0.004f, 0.002f}),
        MAGN(4, new float[]{0.0f, 0.0f, 0.4f});


        private int vChangeType;
        private float[] data;

        Filter(int vChangeType, float[] data) {
            this.vChangeType = vChangeType;
            this.data = data;
        }

        public int getType() {
            return vChangeType;
        }

        public float[] data() {
            return data;
        }

    }
}
