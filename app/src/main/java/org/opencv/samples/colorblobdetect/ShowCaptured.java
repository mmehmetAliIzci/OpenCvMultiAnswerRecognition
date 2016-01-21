package org.opencv.samples.colorblobdetect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ShowCaptured extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_captured);

        mImageView = (ImageView) findViewById(R.id.capturedImg);



        Bitmap temp_bmp = null;
        String filename = this.getIntent().getStringExtra("image");

        try {
            FileInputStream in = this.openFileInput(filename);
            temp_bmp = BitmapFactory.decodeStream(in);
            in.close();
            mImageView.setImageBitmap(temp_bmp);
        }
        catch (Exception e){
            Log.e("Passbitmap", e.toString());
            e.printStackTrace();
        }




    }
}
