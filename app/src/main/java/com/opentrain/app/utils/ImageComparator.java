package com.opentrain.app.utils;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageComparator {

    private static final int SCALED_WIDTH = 100;

    /*Compare two bitmap.
    *
    * @param bmpimg1
    * @param bmpimg2
    * @return Percentage Match.*/
    public double percentageMatch(Bitmap image1, Bitmap image2) {
        image1 = createScaledBitmap(image1);
        image2 = createScaledBitmap(image2);

        ByteBuffer buffer1 = ByteBuffer.allocate(image1.getHeight() * image1.getRowBytes());
        image1.copyPixelsToBuffer(buffer1);
        ByteBuffer buffer2 = ByteBuffer.allocate(image2.getHeight() * image2.getRowBytes());
        image2.copyPixelsToBuffer(buffer2);

        byte[] b1 = buffer1.array();
        byte[] b2 = buffer2.array();
        int match = 0;
        int total = b1.length;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] == b2[i]) {
                match = match + 1;
            }
        }
        double matchPercentage = ((double)(match / total) * 100);
        Log.d("ImageComparator", "match: " + matchPercentage);
        return matchPercentage;
    }

    /*Create scaled bitmap and maintain aspect ration.
    *
    * @param image
    * @param maxWidth
    * @param maxHeight
    * @return Scaled bitmap.*/
    private Bitmap createScaledBitmap(Bitmap image) {
        int  scaled_height = image.getHeight()*SCALED_WIDTH/image.getWidth();
        if (scaled_height > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) scaled_height / (float) scaled_height;

            int finalWidth = scaled_height;
            int finalHeight = scaled_height;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) scaled_height * ratioBitmap);
            } else {
                finalHeight = (int) ((float) scaled_height / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}