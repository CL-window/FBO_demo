package com.cl.slack.fbo_demo.imagefiler;

/**
 * Created by slack
 * on 18/1/14 下午6:12
 */

public class NoFilter extends IFilter {

    @Override
    String obtainVertex() {
        return  "attribute vec4 vPosition;\n" +
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
                "varying vec2 aCoordinate;\n" +
                "void main(){\n" +
                "    gl_FragColor=texture2D(vTexture,aCoordinate);\n" +
                "}";
    }

    @Override
    void onExtraCreated(int mProgram) {

    }
}
