package com.opentrain.app.utils;


import java.util.ArrayList;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;

import org.opencv.imgproc.Imgproc;

import android.app.Activity;

import android.graphics.Bitmap;

import android.util.Log;


public class ImageComparator {


    private Activity context;


    public ImageComparator(Activity context) {

        this.context = context;

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, context,
                mLoaderCallback);

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public double doCompare(Bitmap bmpimg1, Bitmap bmpimg2) {


            bmpimg1 = Bitmap.createScaledBitmap(bmpimg1, 100, 100, true);
            bmpimg2 = Bitmap.createScaledBitmap(bmpimg2, 100, 100, true);
            Mat img1 = new Mat();
            Utils.bitmapToMat(bmpimg1, img1);
            Mat img2 = new Mat();
            Utils.bitmapToMat(bmpimg2, img2);
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
            img1.convertTo(img1, CvType.CV_32F);
            img2.convertTo(img2, CvType.CV_32F);
            Mat hist1 = new Mat();
            Mat hist2 = new Mat();
            MatOfInt histSize = new MatOfInt(180);
            MatOfInt channels = new MatOfInt(0);
            ArrayList<Mat> bgr_planes1 = new ArrayList<>();
            ArrayList<Mat> bgr_planes2 = new ArrayList<>();
            Core.split(img1, bgr_planes1);
            Core.split(img2, bgr_planes2);
            MatOfFloat histRanges = new MatOfFloat(0f, 180f);
            boolean accumulate = false;
            Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
            Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
            Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
            Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
            img1.convertTo(img1, CvType.CV_32F);
            img2.convertTo(img2, CvType.CV_32F);
            hist1.convertTo(hist1, CvType.CV_32F);
            hist2.convertTo(hist2, CvType.CV_32F);
            double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
            Log.d("ImageComparator", "compare: " + compare);

            return compare;



    }

}
