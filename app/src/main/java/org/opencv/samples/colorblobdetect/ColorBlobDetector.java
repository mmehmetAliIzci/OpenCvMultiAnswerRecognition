package org.opencv.samples.colorblobdetect;

import android.graphics.Bitmap;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;


public class ColorBlobDetector {

    //circles
    public static int maxRadius = 26,minRadius = 19;
    public static double dp = 1,minDist = 61,param1= 4,param2=8;


    public static TreeMap <Integer,MatOfPoint> centerXContour;

    //
    public static int SameCenterContour = 0;
    public static int errorRate = 7;
    public static boolean isFound = false;
    public static int meanCenterX = 0;


    // Cache
    Mat mPyrDownMat = new Mat();
    public static Mat rgbaTemp = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    public static Bitmap mBitmap;
    public static double minTreshold = 60;
    public static double maxTreshold = 255;



    private static final String  TAG              = "ColorBlobDetector.java";
    // Lower and Upper bounds for range checking in HSV color space
    public static Scalar mLowerBound = new Scalar(0,0,0);
    public static Scalar mUpperBound = new Scalar(255,255,minTreshold);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.01;

    private static Size temp_size = new Size(9,9);
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process2(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
                //Log.e("Max Area", "wtf.. " + maxArea);
            }

        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                    Core.multiply(contour, new Scalar(4,4), contour);
                    mContours.add(contour);
            }
        }
    }
    public void process3(Mat rgbaImage) {

        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        //Imgproc.medianBlur(rgbaImage,rgbaImage,9);
        Imgproc.cvtColor(mPyrDownMat, mPyrDownMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(mPyrDownMat,mPyrDownMat,50,200,3,true);
        Imgproc.dilate(mPyrDownMat, mPyrDownMat, new Mat());
        Imgproc.erode(mPyrDownMat,mPyrDownMat,new Mat());
        //Imgproc.blur(rgbaImage,rgbaImage,temp_size);

        //Imgproc.Canny(mPyrDownMat,mPyrDownMat,10,100,3,true);

        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);


        //Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        //Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        //Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        //Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.findContours(mPyrDownMat, contours,  new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
                //Log.e("Max Area", "wtf.. " + maxArea);
            }

        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        Log.e(TAG, "Contours count: " + contours.size());
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);

                //Change image view to show what is founded
                Mat m = new Mat();

                Rect temp_rec = Imgproc.boundingRect(contour);

                //Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);
                m = rgbaImage.submat(temp_rec);
                mBitmap = Bitmap.createBitmap(m.width(),m.height(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m,mBitmap);
            }

        }

        /*mContours.clear();
        each = contours.iterator();
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint mMOP = new MatOfPoint();
        while (each.hasNext()) {

            MatOfPoint contour = each.next();
            contour.convertTo(mMOP2f1,CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f1,approx,Imgproc.arcLength(mMOP2f1,true)*0.02,true);
            approx.convertTo(mMOP, CvType.CV_32S);

            if(approx.rows()==4 && Imgproc.isContourConvex(mMOP) ){
                //Log.e("contour", "Contour area" + Imgproc.contourArea(contour) + "Max Area");
                if (Imgproc.contourArea(contour) > maxArea){
                    MatOfPoint temp = new MatOfPoint();
                    approx.convertTo(temp,CvType.CV_32S);
                    Core.multiply(temp, new Scalar(4,4), temp);
                    mContours.add(temp);


                    Rect temp_rec = Imgproc.boundingRect(temp);

                    Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);


                    /*Core.multiply(approx,new Scalar(4,4),approx);
                    Mat src = new Mat(4,1, CvType.CV_32FC2);

                    Log.e(TAG,"" + (int)approx.toList().get(0).y + (int)approx.toList().get(0).x + "\n" +
                            (int)approx.toList().get(1).y +(int)approx.toList().get(1).x + "\n" +
                            (int)approx.toList().get(2).y + (int)approx.toList().get(2).x + "\n" +
                            (int)approx.toList().get(3).y + (int)approx.toList().get(3).x);

                    src.put((int)approx.toList().get(0).y, (int)approx.toList().get(0).x,
                            (int)approx.toList().get(1).y, (int)approx.toList().get(1).x,
                            (int)approx.toList().get(2).y, (int)approx.toList().get(2).x,
                            (int)approx.toList().get(3).y, (int)approx.toList().get(3).x
                            );

                    Mat dst = new Mat(4,1,CvType.CV_32FC2);
                    dst.put(0,0,0,temp.width(),temp.height(),temp.width(),temp.height(),0);

                    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src,dst);
                    Imgproc.warpPerspective(temp,temp,perspectiveTransform, new Size(temp.cols(), temp.rows()));

                    Mat m = new Mat();

                    temp_rec = Imgproc.boundingRect(temp);
                    m = rgbaImage.submat(temp_rec);
                    mBitmap = Bitmap.createBitmap(m.width(),m.height(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(m,mBitmap);

                }

            }

        }*/
    }
    public void process4(Mat rgbaImage) {

        rgbaImage.copyTo(mPyrDownMat);
        Imgproc.blur(mPyrDownMat,mPyrDownMat,temp_size);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
                //Log.e("Max Area", "wtf.. " + maxArea);
            }

        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {

                mContours.add(contour);

                //Change image view to show what is founded
                Mat m = new Mat();

                Rect temp_rec = Imgproc.boundingRect(contour);

                Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);
                m = rgbaImage.submat(temp_rec);
                mBitmap = Bitmap.createBitmap(m.width(),m.height(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m,mBitmap);
            }
        }
    }

    public void processWorking_JustReminders(Mat rgbaImage) {

        //Mat scannedArea = new Mat (rgbaImage,ColorBlobDetectionActivity.scannedRect);
        //Mat scannedArea = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);

        centerXContour = new TreeMap<>();
        //rgbaImage = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);

        rgbaImage.copyTo(mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mPyrDownMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.blur(mPyrDownMat,mPyrDownMat,new Size(25,25));
        Imgproc.adaptiveThreshold(mPyrDownMat,mPyrDownMat,maxTreshold,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,501,50);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        mMask = mPyrDownMat.clone();

        Imgproc.findContours(mMask, contours,  new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Iterator<MatOfPoint> each = contours.iterator();
        mContours.clear();
        Log.e("Contours",contours.size()+"");

        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            Log.e("Contours",Imgproc.boundingRect(contour).size().height +"");
            if (Imgproc.boundingRect(contour).size().height  > 65 ) {
                mContours.add(contour);

                //Draw bounding rect ------------------
                Rect temp_rec = Imgproc.boundingRect(contour);
                Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);


                //Draw mid
                Moments p = Imgproc.moments(contour, false);
                int x = (int) (p.get_m10() / p.get_m00());

                centerXContour.put(x,contour);

            }
        }//end while all contours


    }

    public void process5(Mat rgbaImage) {

        //Mat scannedArea = new Mat (rgbaImage,ColorBlobDetectionActivity.scannedRect);
        //Mat scannedArea = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);
        //rgbaImage = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);



        Imgproc.cvtColor(rgbaImage,mPyrDownMat,Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.medianBlur(rgbaImage,rgbaImage,9);

        //Imgproc.cvtColor(rgbaImage, rgbaImage, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.blur(mPyrDownMat,mPyrDownMat,temp_size);
        Imgproc.GaussianBlur(mPyrDownMat, mPyrDownMat,new Size(5,5), 0);
        //Imgproc.threshold(mPyrDownMat,mPyrDownMat,0,maxTreshold,Imgproc.THRESH_OTSU);
        //Imgproc.threshold(mPyrDownMat,mPyrDownMat,minTreshold,maxTreshold,Imgproc.THRESH_BINARY_INV);

        Imgproc.adaptiveThreshold(mPyrDownMat,mPyrDownMat,maxTreshold,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,75,10);

        //Imgproc.Canny(rgbaImage,rgbaImage,minTreshold,maxTreshold,3,true);
        //Imgproc.dilate(rgbaImage, rgbaImage, new Mat());
        //Imgproc.erode(rgbaImage,rgbaImage,new Mat());



        //Imgproc.Canny(mPyrDownMat,mPyrDownMat,10,100,3,true);

        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);


        //Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        //Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        //Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        //Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Imgproc.findContours(rgbaImage, contours,  new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        /*
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
                //Log.e("Max Area", "wtf.. " + maxArea);
            }

        }*/
/*
        Iterator<MatOfPoint> each = contours.iterator();
        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();


        //Center Calculation init
        meanCenterX = 0;
        SameCenterContour = 0;
        Point temp_point = new Point(0,0);

        int temp_point_y = 0;



        while (each.hasNext()) {
            MatOfPoint contour = each.next();

            //Log.e(TAG, "Sizes: " + Imgproc.contourArea(contour) + " Max: " + mMinContourArea*maxArea);
            if (Imgproc.contourArea(contour) > 0) {
                mContours.add(contour);


                //Change image view to show what is founded
                Mat m = new Mat();

                //Draw bounding rect
                Rect temp_rec = Imgproc.boundingRect(contour);
                Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);

                //Log.e(TAG, "temprec x: " + temp_rec.tl().x + " temrec width: " + temp_rec.width);
                //Log.e(TAG, "temprec : " + temp_rec.tl().y + " temrec width: " + temp_rec.height);

                MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
                MatOfPoint2f approx = new MatOfPoint2f();
*/

                /*contour.convertTo(mMOP2f1,CvType.CV_32FC2);
                Imgproc.approxPolyDP(mMOP2f1,approx,Imgproc.arcLength(mMOP2f1,true)*0.02,true);

                if (approx.rows() >= 4){
                    Point tr = approx.toList().get(0);
                    Point br = approx.toList().get(1);
                    Point bl = approx.toList().get(2);
                    Point tl = approx.toList().get(3);

                    //temp_rec.tl().x  -= temp_rec.width;
                    //temp_rec.tl().y -= temp_rec.height;
                    Imgproc.circle(rgbaImage,tr,1,new Scalar(255,0,0,255),4);
                    Imgproc.circle(rgbaImage,br,1,new Scalar(255,100,0,255),4);
                    Imgproc.circle(rgbaImage,bl,1,new Scalar(255,100,250,255),4);
                    Imgproc.circle(rgbaImage,tl,1,new Scalar(255,100,100,255),4);

                }*/
/*
                Point tr = new Point(temp_rec.tl().x+temp_rec.size().width,temp_rec.tl().y);
                Point bl = new Point(temp_rec.br().x-temp_rec.size().width,temp_rec.br().y);

                Imgproc.circle(rgbaImage,temp_rec.tl(),1,new Scalar(0,0,255,255),4);
                Imgproc.circle(rgbaImage,tr,1,new Scalar(255,255,0,255),4);
                Imgproc.circle(rgbaImage,temp_rec.br(),1,new Scalar(255,0,0,255),4);
                Imgproc.circle(rgbaImage,bl,1,new Scalar(255,0,255,255),4);

                //Draw mid

                Moments p = Imgproc.moments(contour, false);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());


                //Check if they are in the same vertical line

                meanCenterX += x;
*/
                /*
                if (contours.size() > 14) {
                    if (temp_point.x == x)
                        SameCenterContour++;

                }*/
/*
                Imgproc.circle(rgbaImage, new Point(x, y), 1, new Scalar(255,49,0,255),4);
                Imgproc.line(rgbaImage,new Point(x,y), new Point(x,0),new Scalar(255,49,0,255),4);
                centers.add(x);
                /*
                //set preview
                m = rgbaImage.submat(temp_rec);
                mBitmap = Bitmap.createBitmap(m.width(),m.height(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m,mBitmap);*//*
            }
        }//end while all contours*/




        /*mContours.clear();
        each = contours.iterator();
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint mMOP = new MatOfPoint();
        while (each.hasNext()) {

            MatOfPoint contour = each.next();
            contour.convertTo(mMOP2f1,CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f1,approx,Imgproc.arcLength(mMOP2f1,true)*0.02,true);
            approx.convertTo(mMOP, CvType.CV_32S);

            if(approx.rows()==4 && Imgproc.isContourConvex(mMOP) ){
                //Log.e("contour", "Contour area" + Imgproc.contourArea(contour) + "Max Area");
                if (Imgproc.contourArea(contour) > maxArea){
                    MatOfPoint temp = new MatOfPoint();
                    approx.convertTo(temp,CvType.CV_32S);
                    Core.multiply(temp, new Scalar(4,4), temp);
                    mContours.add(temp);


                    Rect temp_rec = Imgproc.boundingRect(temp);

                    Imgproc.rectangle(rgbaImage,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);


                    /*Core.multiply(approx,new Scalar(4,4),approx);
                    Mat src = new Mat(4,1, CvType.CV_32FC2);

                    Log.e(TAG,"" + (int)approx.toList().get(0).y + (int)approx.toList().get(0).x + "\n" +
                            (int)approx.toList().get(1).y +(int)approx.toList().get(1).x + "\n" +
                            (int)approx.toList().get(2).y + (int)approx.toList().get(2).x + "\n" +
                            (int)approx.toList().get(3).y + (int)approx.toList().get(3).x);

                    src.put((int)approx.toList().get(0).y, (int)approx.toList().get(0).x,
                            (int)approx.toList().get(1).y, (int)approx.toList().get(1).x,
                            (int)approx.toList().get(2).y, (int)approx.toList().get(2).x,
                            (int)approx.toList().get(3).y, (int)approx.toList().get(3).x
                            );

                    Mat dst = new Mat(4,1,CvType.CV_32FC2);
                    dst.put(0,0,0,temp.width(),temp.height(),temp.width(),temp.height(),0);

                    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src,dst);
                    Imgproc.warpPerspective(temp,temp,perspectiveTransform, new Size(temp.cols(), temp.rows()));

                    Mat m = new Mat();

                    temp_rec = Imgproc.boundingRect(temp);
                    m = rgbaImage.submat(temp_rec);
                    mBitmap = Bitmap.createBitmap(m.width(),m.height(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(m,mBitmap);

                }

            }

        }*/
        Mat circles = new Mat();


        Imgproc.HoughCircles(mPyrDownMat,circles,Imgproc.CV_HOUGH_GRADIENT,dp,minDist,param1,param2,minRadius,maxRadius);

        //Imgproc.HoughCircles(rgbaImage,circles,Imgproc.CV_HOUGH_GRADIENT,1,20,23,46,0,0);
        Log.e("Circles", "Total circle count:" + circles.cols());
        Mat circleBoundRect = new Mat();
        for (int i = 0; i < circles.cols(); i++) {
            double mCircle[] = circles.get(0,i);

            int x = (int) Math.round(mCircle[0]);
            int y = (int) Math.round(mCircle[1]);
            int r = (int) Math.round(mCircle[2]);

            Point center = new Point(x ,y);

            Point tl = new Point(x-r,y-r);
            Point br = new Point(x+r,y+r);

            Rect circleBoundRect_temp = new Rect(tl,br);
            Log.e("Circles","x: "+ x + "y: "+y + "r: "+r);

            try {
                circleBoundRect = mPyrDownMat.submat(circleBoundRect_temp);
            }
            catch (Exception e){
                Log.e("Circles Exception",e.toString());
            }
            double percentage = Core.countNonZero(circleBoundRect)/(circleBoundRect.size().width*circleBoundRect.size().height);
            if (percentage> 0.6){
                Imgproc.rectangle(rgbaImage,tl,br,new Scalar(247,0,255),3);
            }

            else if (percentage < 0.6 && percentage>0.3){
                Imgproc.rectangle(rgbaImage,tl,br,new Scalar(0,255,0),3);
            }

            //Log.e("Circles",""+Core.countNonZero(circleBoundRect)/(circleBoundRect.size().width*circleBoundRect.size().height));
            //draw circle
            //Imgproc.circle(rgbaImage,center,r,new Scalar(57,58,153),4);
            //draw center
            //Imgproc.circle(rgbaImage,center,3,new Scalar(57,58,153),5);



        }

        mBitmap = Bitmap.createBitmap(rgbaImage.width(),rgbaImage.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaImage,mBitmap);

    }

    public void process(Mat rgbaImage) {

        //Mat scannedArea = new Mat (rgbaImage,ColorBlobDetectionActivity.scannedRect);
        //Mat scannedArea = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);
        rgbaImage = rgbaImage.submat(ColorBlobDetectionActivity.scannedRect);

        Imgproc.cvtColor(rgbaImage,mPyrDownMat,Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(mPyrDownMat, mPyrDownMat,new Size(5,5), 0);

        Imgproc.adaptiveThreshold(mPyrDownMat,mPyrDownMat,maxTreshold,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,75,10);

        mBitmap = Bitmap.createBitmap(rgbaImage.width(),rgbaImage.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaImage,mBitmap);
        isFound = true;

    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }

    public void takePicAndProcess (Mat rgbaImage){

        Mat warped = rgbaImage.clone();

        //Try to identify the first Top Left and BottomLeft Contours
        Point tl_canvas = new Point(0,0);
        MatOfPoint actual_canvas_tl = new MatOfPoint();
        Point tr_canvas  = new Point(0,0);
        MatOfPoint actual_canvas_tr =  new MatOfPoint();
        Point br_canvas  = new Point(0,0);
        MatOfPoint actual_canvas_br =  new MatOfPoint();
        Point bl_canvas  = new Point(0,0);
        MatOfPoint actual_canvas_bl =  new MatOfPoint();

        Moments p = Imgproc.moments(centerXContour.get(centerXContour.keySet().toArray()[0]), false);
        int firsty = (int) (p.get_m01() / p.get_m00());

        p = Imgproc.moments(centerXContour.get(centerXContour.keySet().toArray()[1]), false);
        int secondy = (int) (p.get_m01() / p.get_m00());

        p = Imgproc.moments(centerXContour.get(centerXContour.lastKey()), false);
        int lasty = (int) (p.get_m01() / p.get_m00());

        p = Imgproc.moments(centerXContour.get(centerXContour.keySet().toArray()[(centerXContour.keySet().size()-2)]), false);
        int lastSecy = (int) (p.get_m01() / p.get_m00());

        Log.e("Centers","First Key: " +centerXContour.keySet().toArray()[0] +"," + firsty + "annd Second key:"+ centerXContour.keySet().toArray()[1] +","+ secondy );
        Log.e("Centers","Second LAst key:"+ centerXContour.keySet().toArray()[(centerXContour.keySet().size()-2)] +"," +lastSecy + "and Last Key: " +centerXContour.lastKey()+"," +lasty  );


        if (firsty <= secondy){
            actual_canvas_tl = centerXContour.get(centerXContour.keySet().toArray()[0]);
            actual_canvas_bl = centerXContour.get(centerXContour.keySet().toArray()[1]);
        }
        else {
            actual_canvas_tl = centerXContour.get(centerXContour.keySet().toArray()[1]);
            actual_canvas_bl = centerXContour.get(centerXContour.keySet().toArray()[0]);
        }
        if (lastSecy <= lasty){
            actual_canvas_br = centerXContour.get(centerXContour.lastKey());
            actual_canvas_tr = centerXContour.get(centerXContour.keySet().toArray()[(centerXContour.keySet().size()-2)]);
        }
        else {
            actual_canvas_br = centerXContour.get(centerXContour.keySet().toArray()[(centerXContour.keySet().size()-2)]);
            actual_canvas_tr = centerXContour.get(centerXContour.lastKey());
        }
        int y= 0;
        List<Point> normalPointsList = new ArrayList<>();
        List<Point> transformedPointsList = new ArrayList<>();

        //calc tl point
        Rect temp_rec = Imgproc.boundingRect(actual_canvas_tl);
        tl_canvas = temp_rec.br();
        normalPointsList.add(tl_canvas);
        Imgproc.circle(rgbaImage,tl_canvas,1,new Scalar(144,195,212),10);

        //calc tr point
        temp_rec = Imgproc.boundingRect(actual_canvas_tr);
        y = (int) (temp_rec.tl().y +temp_rec.height);
        tr_canvas = new Point(temp_rec.tl().x,y);
        normalPointsList.add(tr_canvas);
        Imgproc.circle(rgbaImage,tr_canvas,1,new Scalar(211,76,245),10);

        //calc br point
        temp_rec = Imgproc.boundingRect(actual_canvas_br);
        y = (int) (temp_rec.tl().y +temp_rec.height);
        br_canvas = new Point(temp_rec.tl().x,y);
        normalPointsList.add(br_canvas);
        Imgproc.circle(rgbaImage,br_canvas,1,new Scalar(255,0,0,255),10);

        //calc bl point
        temp_rec = Imgproc.boundingRect(actual_canvas_bl);
        bl_canvas = temp_rec.br();
        normalPointsList.add(bl_canvas);
        Imgproc.circle(rgbaImage,bl_canvas,1,new Scalar(217,245,76),10);


        int maxWidthBrBl = (int) Math.sqrt(Math.pow(br_canvas.x-bl_canvas.x,2) + Math.pow(br_canvas.y-bl_canvas.y,2));
        int maxWidthTrTl = (int) Math.sqrt(Math.pow(tr_canvas.x-tl_canvas.x,2) + Math.pow(tr_canvas.y-tl_canvas.y,2));

        int maxHeightTrBr = (int) Math.sqrt(Math.pow(tr_canvas.x-br_canvas.x,2) + Math.pow(tr_canvas.y-br_canvas.y,2));
        int maxHeightTlBl = (int) Math.sqrt(Math.pow(tl_canvas.x-bl_canvas.x,2) + Math.pow(tl_canvas.y-bl_canvas.y,2));



        int maxWidth = Math.max(maxWidthBrBl,maxWidthTrTl);
        int maxHeight = Math.max(maxHeightTlBl,maxHeightTrBr);
        Log.e("Dimensions","Maxw1: " + maxWidth + "Maxh2: "+maxHeight);


        transformedPointsList.add(new Point(0,0));
        transformedPointsList.add(new Point(maxWidth,0));
        transformedPointsList.add(new Point(maxWidth,maxHeight));
        transformedPointsList.add(new Point(0,maxHeight));


        Log.e("NormalPoints",normalPointsList.toString());
        Log.e("TransformedPts",transformedPointsList.toString());

        Mat normalPoints = new Mat();
        normalPoints = Converters.vector_Point2f_to_Mat(normalPointsList);
        Log.e("NormalPoints MAT",normalPoints.toString());

        Mat transformedPoints = new Mat();
        transformedPoints = Converters.vector_Point2f_to_Mat(transformedPointsList);
        Log.e("TransformedPoints MAT",transformedPoints.toString());

        /*
        int marginFromRealTop       = (warped.height()*10/100);
        int marginBetweenCenters    = (warped.height()*13/100);
        int radius                  = (warped.height()*6/100);
        int marginFromRealSide      = (warped.width()*2/100);

        Log.e("NewCirclePoints","marginFromRealTop "   + marginFromRealTop+
                "marginBetweenCenters " +marginBetweenCenters+
                "radius "  +           radius+
                "marginFromRealSide "+ marginFromRealSide+
                "warpedHeight: " + warped.height()+
                "warpedWidth: " + warped.width()
        );
        int firstCenterx = marginFromRealSide+radius;
        int firstCentery =marginFromRealTop+radius;

        Log.e("NewCirclePoints", new Point(firstCenterx,firstCentery).toString() +
                new Point(firstCenterx,firstCentery+(marginBetweenCenters*1)).toString()+
                new Point(firstCenterx,firstCentery+(marginBetweenCenters*2)).toString()+
                new Point(firstCenterx,firstCentery+(marginBetweenCenters*3)).toString()+
                new Point(firstCenterx,firstCentery+(marginBetweenCenters*4)).toString());
        */
        Mat H = Imgproc.getPerspectiveTransform(normalPoints,transformedPoints);
        Imgproc.warpPerspective(rgbaImage,warped,H,new Size(maxWidth,maxHeight));

        findContoursAndMarkCenters(warped);

        //Imgproc.circle(warped,new Point(firstCenterx,20),1,new Scalar(255,0,0),10);
        //Imgproc.circle(warped,new Point(firstCenterx,firstCentery+(marginBetweenCenters*1)),1,new Scalar(255,0,0),10);
        //Imgproc.circle(warped,new Point(firstCenterx,firstCentery+(marginBetweenCenters*2)),1,new Scalar(255,0,0),10);
        //Imgproc.circle(warped,new Point(firstCenterx,firstCentery+(marginBetweenCenters*3)),1,new Scalar(255,0,0),10);
        //Imgproc.circle(warped,new Point(firstCenterx,firstCentery+(marginBetweenCenters*4)),1,new Scalar(0,0,255),10);

        mBitmap = Bitmap.createBitmap(warped.width(),warped.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(warped,mBitmap);
    }

    public void findContoursAndMarkCenters(Mat rgbaImg){

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<Point> pointerCenters = new ArrayList<>();
        Mat mProccessed = new Mat();


        Imgproc.cvtColor(rgbaImg, mProccessed, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.blur(mProccessed,mProccessed,new Size(25,25));
        //Imgproc.threshold(rgbaImg,rgbaImg,0,maxTreshold,Imgproc.THRESH_OTSU);


        //Imgproc.GaussianBlur(rgbaImg, rgbaImg,new Size(25,25), 0);

        Imgproc.adaptiveThreshold(mProccessed,mProccessed,maxTreshold,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,75,25);

        Mat mask = mProccessed.clone();

        Imgproc.findContours(mask, contours,  new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Iterator<MatOfPoint> each = contours.iterator();


        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            //Log.e("Contours",Imgproc.boundingRect(contour).size().width +"");
            //Log.e(TAG, "Sizes: " + Imgproc.contourArea(contour));
            if (Imgproc.boundingRect(contour).size().height  > 65 ) {

                //Draw bounding rect ------------------
                Rect temp_rec = Imgproc.boundingRect(contour);
                Imgproc.rectangle(rgbaImg,temp_rec.tl(),temp_rec.br(),ColorBlobDetectionActivity.CONTOUR_COLOR,6);

                //Log.e(TAG, "temprec x: " + temp_rec.tl().x + " temrec width: " + temp_rec.width);
                //Log.e(TAG, "temprec : " + temp_rec.tl().y + " temrec width: " + temp_rec.height);

                //MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
                //MatOfPoint2f approx = new MatOfPoint2f();


                /*contour.convertTo(mMOP2f1,CvType.CV_32FC2);
                Imgproc.approxPolyDP(mMOP2f1,approx,Imgproc.arcLength(mMOP2f1,true)*0.02,true);

                if (approx.rows() >= 4){
                    Point tr = approx.toList().get(0);
                    Point br = approx.toList().get(1);
                    Point bl = approx.toList().get(2);
                    Point tl = approx.toList().get(3);

                    //temp_rec.tl().x  -= temp_rec.width;
                    //temp_rec.tl().y -= temp_rec.height;
                    Imgproc.circle(rgbaImage,tr,1,new Scalar(255,0,0,255),4);
                    Imgproc.circle(rgbaImage,br,1,new Scalar(255,100,0,255),4);
                    Imgproc.circle(rgbaImage,bl,1,new Scalar(255,100,250,255),4);
                    Imgproc.circle(rgbaImage,tl,1,new Scalar(255,100,100,255),4);

                }*/

                /*
                Imgproc.circle(rgbaImage,temp_rec.tl(),1,new Scalar(0,0,255,255),4);
                Imgproc.circle(rgbaImage,tr,1,new Scalar(255,255,0,255),4);
                Imgproc.circle(rgbaImage,temp_rec.br(),1,new Scalar(255,0,0,255),4);
                Imgproc.circle(rgbaImage,bl,1,new Scalar(255,0,255,255),4);*/

                //Draw mid
                Moments p = Imgproc.moments(contour, false);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());

                Imgproc.circle(rgbaImg, new Point(x, y), 1, new Scalar(255,49,0,255),4);
                pointerCenters.add(new Point(x, y));
                Log.e("Centers","x: " + x +"y: "+ y);

                /*Imgproc.circle(rgbaImage, new Point(x, y-(ColorBlobDetectionActivity.pointerRectHeight/4)), 1, new Scalar(255,255,0,255),5);
                Imgproc.circle(rgbaImage, new Point(x, y-((ColorBlobDetectionActivity.pointerRectHeight)*3/9)), 1, new Scalar(255,255,0,255),5);
                Imgproc.circle(rgbaImage, new Point(x, y-((ColorBlobDetectionActivity.pointerRectHeight)*4/9)), 1, new Scalar(255,255,0,255),5);
                Imgproc.circle(rgbaImage, new Point(x, y-((ColorBlobDetectionActivity.pointerRectHeight)*5/9)), 1, new Scalar(255,255,0,255),5);
                Imgproc.circle(rgbaImage, new Point(x, y-((ColorBlobDetectionActivity.pointerRectHeight)*6/9)), 1, new Scalar(255,255,0,255),5);*/
                //Imgproc.line(rgbaImage,new Point(x,y), new Point(x,y-(ColorBlobDetectionActivity.pointerRectHeight/4)),new Scalar(255,49,0,255),4);

            }
        }//end while all contours

        for (Point p :
                pointerCenters) {

            int firstMargin = (int) ((rgbaImg.size().height/100)*28);
            int betweenMargin = (int) ((rgbaImg.size().height/100)*14);
            int radius = (int) ((rgbaImg.size().height/100)*4);

            //A
            Rect roi = new Rect(new Point(p.x-radius, p.y-firstMargin-radius),new Point(p.x+radius, p.y-firstMargin+radius));
            Mat boundRectBubble = mProccessed.submat(roi);
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin), radius, new Scalar(0,255,0,255),4);
            double percentage =(double)Core.countNonZero(boundRectBubble)/(boundRectBubble.size().width*boundRectBubble.size().height);
            if(percentage>=0.3 ){
                Imgproc.rectangle(rgbaImg,roi.tl(),roi.br(),new Scalar(0,255,0,255),6);
            }
            Log.e("CoreChecking", (double)Core.countNonZero(boundRectBubble) +" Area: " + (boundRectBubble.size().width*boundRectBubble.size().height));


            //B
            roi = new Rect(new Point(p.x-radius, p.y-firstMargin-betweenMargin-radius),new Point(p.x+radius, p.y-firstMargin-betweenMargin+radius));
            boundRectBubble = mProccessed.submat(roi);
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin), radius, new Scalar(0,255,0,255),4);
             percentage =(double)Core.countNonZero(boundRectBubble)/(boundRectBubble.size().width*boundRectBubble.size().height);
            if(percentage>=0.3 ){
                Imgproc.rectangle(rgbaImg,roi.tl(),roi.br(),new Scalar(0,255,0,255),6);
            }
            Log.e("CoreChecking", (double)Core.countNonZero(boundRectBubble) +" Area: " + (boundRectBubble.size().width*boundRectBubble.size().height));
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin-betweenMargin), radius, new Scalar(255,49,0,255),4);


            //C
            roi = new Rect(new Point(p.x-radius, p.y-firstMargin-betweenMargin-betweenMargin-radius),new Point(p.x+radius, p.y-firstMargin-betweenMargin-betweenMargin+radius));
            boundRectBubble = mProccessed.submat(roi);
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin), radius, new Scalar(0,255,0,255),4);
            percentage =(double)Core.countNonZero(boundRectBubble)/(boundRectBubble.size().width*boundRectBubble.size().height);
            if(percentage>=0.3 ){
                Imgproc.rectangle(rgbaImg,roi.tl(),roi.br(),new Scalar(0,255,0,255),6);
            }
            Log.e("CoreChecking", (double)Core.countNonZero(boundRectBubble) +" Area: " + (boundRectBubble.size().width*boundRectBubble.size().height));

            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin-betweenMargin-betweenMargin), radius, new Scalar(255,49,0,255),4);

            //D
            roi = new Rect(new Point(p.x-radius, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin-radius),new Point(p.x+radius, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin+radius));
            boundRectBubble = mProccessed.submat(roi);
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin), radius, new Scalar(0,255,0,255),4);
            percentage =(double)Core.countNonZero(boundRectBubble)/(boundRectBubble.size().width*boundRectBubble.size().height);
            if(percentage>=0.3 ){
                Imgproc.rectangle(rgbaImg,roi.tl(),roi.br(),new Scalar(0,255,0,255),6);
            }
            Log.e("CoreChecking", (double)Core.countNonZero(boundRectBubble) +" Area: " + (boundRectBubble.size().width*boundRectBubble.size().height));

            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin), radius, new Scalar(255,49,0,255),4);


            //E
            roi = new Rect(new Point(p.x-radius, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin-betweenMargin-radius),new Point(p.x+radius, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin-betweenMargin+radius));
            boundRectBubble = mProccessed.submat(roi);
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin), radius, new Scalar(0,255,0,255),4);
            percentage =(double)Core.countNonZero(boundRectBubble)/(boundRectBubble.size().width*boundRectBubble.size().height);
            if(percentage>=0.3 ){
                Imgproc.rectangle(rgbaImg,roi.tl(),roi.br(),new Scalar(0,255,0,255),6);
            }
            Log.e("CoreChecking", (double)Core.countNonZero(boundRectBubble) +" Area: " + (boundRectBubble.size().width*boundRectBubble.size().height));
            //Imgproc.circle(rgbaImg, new Point(p.x, p.y-firstMargin-betweenMargin-betweenMargin-betweenMargin-betweenMargin), radius, new Scalar(255,49,0,255),4);
        }

    }


}
