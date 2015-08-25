package com.opentrain.app.utils;

/**
 * Created by noam on 07/06/15.
 */
public class TimeUtils {

    public static String getFormattedTime() {
        return android.text.format.DateFormat.format("hh:mm:ss dd-MM-yyyy", new java.util.Date()).toString();
    }
}
