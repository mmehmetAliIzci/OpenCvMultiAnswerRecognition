package org.opencv.samples.colorblobdetect;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ColorBlobDetectionActivity extends Activity implements  CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    public static Scalar         CONTOUR_COLOR;
    public static Scalar         AREA_COLOR;

    //after added
    public static Scalar         CORNER_COLOR;
    public ImageView mImageView;
    public TextView par1Txt,maxTxt,distTxt,minRTxt,par2Txt;
    public static int sidemargin,topmargin,pointerRectHeight,pointerRectWidth;
    public static Rect scannedRect = new Rect();



    private CameraBridgeViewBase mOpenCvCameraView;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.color_blob_detection_surface_view);


        /*par1Txt = (TextView) findViewById(R.id.par1Txt);
        maxTxt = (TextView) findViewById(R.id.maxTxt);
        distTxt= (TextView) findViewById(R.id.distTxt);
        minRTxt = (TextView) findViewById(R.id.minRTxt);
        par2Txt = (TextView) findViewById(R.id.par2Txt);*/

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();

        display.getSize(size);

        int widthScreen = size.x;
        int heightScreen = size.y;

        sidemargin = widthScreen / 10 ;
        topmargin = heightScreen / 10;

        pointerRectHeight = 4 * (heightScreen / 6);
        pointerRectWidth = 8 * (widthScreen / 10 );

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(0,255,0,0);
        AREA_COLOR = new Scalar(255,0,255,0);
        CORNER_COLOR = new Scalar(0,255,0,0);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

   /* public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }*/

    public Mat onCameraFrame1(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());


            Point p1 = new Point(328.0,168.0);
            Point p2 = new Point(737.0,313.0);


            Imgproc.rectangle(mRgba,p1,p2,new Scalar(255,0,255,0),4);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mImageView.setImageBitmap(ColorBlobDetector.mBitmap);

            }
        });

        return mRgba;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        Point tl = new Point( sidemargin, topmargin);
        Point br = new Point( topmargin+ pointerRectWidth , sidemargin+ pointerRectHeight);
        scannedRect = new Rect(tl,br);
        //Imgproc.rectangle(mRgba,tl,br,new Scalar(255,0,255,0),4);


        mDetector.processWorking_JustReminders(mRgba);

        if (mDetector.getContours().size() == 19){
            mDetector.takePicAndProcess(mRgba);
            takePicture();
        }
        //mDetector.process5(ColorBlobDetector.rgbaTemp);
        //mDetector.process5(mRgba);
        /*
        if(ColorBlobDetector.isFound){
            Intent i = new Intent(ColorBlobDetectionActivity.this,ShowCaptured.class);
            i.putExtra("data",ColorBlobDetector.mBitmap);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }*/


        return mRgba;
    }

    public Mat onCameraFrame3(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        /*if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());


            Point p1 = new Point(328.0,168.0);
            Point p2 = new Point(737.0,313.0);


            Imgproc.rectangle(mRgba,p1,p2,new Scalar(255,0,255,0),4);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }*/

        mDetector.process(mRgba);

        List<MatOfPoint> contours = mDetector.getContours();
        Log.e(TAG, "Contours count: " + contours.size());



        Point p1 = new Point(328.0,168.0);
        Point p2 = new Point(737.0,313.0);


        //Imgproc.rectangle(mRgba,p1,p2,new Scalar(255,0,255,0),4);

        Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);

        Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mImageView.setImageBitmap(ColorBlobDetector.mBitmap);

            }
        });

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public void processDistPlus(View v){

        ColorBlobDetector.minDist++;

        distTxt.setText(String.valueOf(ColorBlobDetector.minDist));

    }

    public void processDistMinus(View v){

        ColorBlobDetector.minDist--;

        distTxt.setText(String.valueOf(ColorBlobDetector.minDist));

    }

    public void processMinRPlus(View v){

        ColorBlobDetector.minRadius++;

        minRTxt.setText(String.valueOf(ColorBlobDetector.minRadius));

    }

    public void processMinRMinus(View v){
        if (ColorBlobDetector.minRadius >0)
            ColorBlobDetector.minRadius--;
        minRTxt.setText(String.valueOf(ColorBlobDetector.minRadius));

    }


    public void takePicture(){

        Intent i = new Intent(ColorBlobDetectionActivity.this,ShowCaptured.class);

        //mDetector.process5(ColorBlobDetector.rgbaTemp);

        try {
            String filename = "temp.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            Bitmap mBitmap = ColorBlobDetector.mBitmap;
            mBitmap.compress(Bitmap.CompressFormat.PNG,100,stream);

            //Cleaning
            stream.close();
            mBitmap.recycle();

            //Intent stuff
            i.putExtra("image",filename);
        }
        catch (Exception e){
            Log.e("Passbitmap", e.toString());
            e.printStackTrace();
        }

        i.putExtra("String","Fuck This! ! We came bitches, annd there is no coming back hahah !");
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void processParPlus(View v){
        ColorBlobDetector.param1++;
        par1Txt.setText(String.valueOf(ColorBlobDetector.param1));
    }
    public void processParMinus(View v){
        if (ColorBlobDetector.param1 >0)
            ColorBlobDetector.param1--;
        par1Txt.setText(String.valueOf(ColorBlobDetector.param1));
    }
    public void processPar2Plus(View v){
        ColorBlobDetector.param2++;
        par2Txt.setText(String.valueOf(ColorBlobDetector.param2));
    }
    public void processPar2Minus(View v){
        if (ColorBlobDetector.param2 >0)
            ColorBlobDetector.param2--;
        par2Txt.setText(String.valueOf(ColorBlobDetector.param2));
    }

    public void processMaxRPlus(View v){
        ColorBlobDetector.maxRadius++;
        maxTxt.setText(String.valueOf(ColorBlobDetector.maxRadius));
    }
    public void processMaxRMinus(View v){
        if (ColorBlobDetector.maxRadius >0)
            ColorBlobDetector.maxRadius--;
        maxTxt.setText(String.valueOf(ColorBlobDetector.maxRadius));
    }

}
