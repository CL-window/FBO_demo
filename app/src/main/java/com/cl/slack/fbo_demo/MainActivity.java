package com.cl.slack.fbo_demo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.cl.slack.fbo_demo.egl.FrameBuffer;
import com.cl.slack.fbo_demo.egl.GLESBackEnv;
import com.cl.slack.fbo_demo.imagefiler.ColorFiler;
import com.cl.slack.fbo_demo.imagefiler.IFilter;
import com.cl.slack.fbo_demo.imagefiler.NoFilter;

public class MainActivity extends AppCompatActivity {

    ImageView imageSrc, imageFilter1, imageFilter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageSrc = findViewById(R.id.fbo_image_src);
        imageFilter1 = findViewById(R.id.fbo_image_filter1);
        imageFilter2 = findViewById(R.id.fbo_image_filter2);

    }

    /**
     * 选择图片
     */
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            obtainBitmap(data);
        }
    }

    private void obtainBitmap(Intent data) {
        Uri selectedImage = data.getData();
        if(selectedImage == null) {
            return;
        }
        String[] filePathColumns = {MediaStore.Images.Media.DATA};
        Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
        if(c == null) {
            return;
        }
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePathColumns[0]);
        String imgPath = c.getString(columnIndex);
        Log.e("slack", "img->" + imgPath);
        // image src
        Bitmap bmp = BitmapFactory.decodeFile(imgPath);
        imageSrc.setImageBitmap(bmp);


        int w =bmp.getWidth();
        int h =bmp.getHeight();
        GLESBackEnv backEnv = new GLESBackEnv(w, h);
        backEnv.setThreadOwner(getMainLooper().getThread().getName());
        // image filter 1
        long start = System.currentTimeMillis();
        showImageFilter1(bmp, backEnv);
        long end = System.currentTimeMillis();
        Log.i("slack", "bitmap cost: " + (end - start));

        start = System.currentTimeMillis();
        showImageFilter2(bmp, backEnv);
        end = System.currentTimeMillis();
        Log.i("slack", "FBO cost: " + (end - start));

        c.close();
    }

    /**
     * bitmap 的数据是从第一行开始存储
     * 处理 framebuffer 数据需要上下颠倒一下
     */
    private void showImageFilter2(Bitmap bmp, GLESBackEnv backEnv) {
        int w =bmp.getWidth();
        int h =bmp.getHeight();

        NoFilter noFilter = new NoFilter();
        noFilter.setBitmap(bmp);
        noFilter.onCreate();
        noFilter.onSizeChange(w, h);

        // bind framebuffer
        FrameBuffer buffer = new FrameBuffer();
        buffer.create(w, h);
        buffer.beginDrawToFrameBuffer();
        noFilter.onDrawFrame();
        buffer.endDrawToFrameBuffer();

        // image filter 2, use textureId form framebuffer
        ColorFiler colorFiler = new ColorFiler(ColorFiler.Filter.GRAY);
        colorFiler.setTextureId(buffer.getTextureId());
        colorFiler.onCreate();
        colorFiler.onSizeChange(w, h);
        float[] coord=new float[]{
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };
        colorFiler.setTexBuffer(coord);

        colorFiler.onDrawFrame();

        bmp = backEnv.getBitmap();
        imageFilter2.setImageBitmap(bmp);

        buffer.release(true);
    }

    private void showImageFilter1(Bitmap bmp, GLESBackEnv backEnv) {
        int w =bmp.getWidth();
        int h =bmp.getHeight();

        ColorFiler colorFiler = new ColorFiler(ColorFiler.Filter.WARM);
        // filter 1
        colorFiler.setBitmap(bmp);
        colorFiler.onCreate();
        colorFiler.onSizeChange(w, h);
        colorFiler.onDrawFrame();
        bmp = backEnv.getBitmap();

        // filter 2
        colorFiler.setFilter(ColorFiler.Filter.GRAY);
        colorFiler.setBitmap(bmp);
        colorFiler.onDrawFrame();
        bmp = backEnv.getBitmap();
        imageFilter1.setImageBitmap(bmp);


    }
}
