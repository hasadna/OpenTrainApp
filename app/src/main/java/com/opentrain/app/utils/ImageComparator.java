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
    * @return Percentage difference.*/
    public double compareBitmap(Bitmap bmpimg1, Bitmap bmpimg2) {
        bmpimg1 = createScaledBitmap(bmpimg1, SCALED_WIDTH, SCALED_WIDTH);
        bmpimg2 = createScaledBitmap(bmpimg2, SCALED_WIDTH, SCALED_WIDTH);

        ByteBuffer buffer1 = ByteBuffer.allocate(bmpimg1.getHeight() * bmpimg1.getRowBytes());
        bmpimg1.copyPixelsToBuffer(buffer1);
        ByteBuffer buffer2 = ByteBuffer.allocate(bmpimg2.getHeight() * bmpimg2.getRowBytes());
        bmpimg2.copyPixelsToBuffer(buffer2);
        int match = 0;
        int total = 0;
        byte[] b1 = buffer1.array();
        byte[] b2 = buffer2.array();

        for (int x = 0; x < b1.length; x++) {
            if (b1[x] == b2[x]) {
                match = match + 1;
            }
            total = total + 1;
        }
        double compare = ((double)(match / total) * 100);
        Log.d("ImageComparator", "compare: " + compare);
        return compare;
    }

    /*Create scaled bitmap and maintain aspect ration.
    *
    * @param image
    * @param maxWidth
    * @param maxHeight
    * @return Scaled bitmap.*/
    private Bitmap createScaledBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}