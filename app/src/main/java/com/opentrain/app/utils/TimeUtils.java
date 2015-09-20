package com.opentrain.app.utils;

import java.util.Date;

/**
 * Created by noam on 07/06/15.
 */
public class TimeUtils {

    public static String getFormattedTime() {
        return android.text.format.DateFormat.format("HH:mm:ss dd-MM-yyyy", new Date()).toString();
    }

    public static String getFormattedTime(long unixTimeMs) {
        return android.text.format.DateFormat.format("HH:mm:ss dd-MM-yyyy", new Date(unixTimeMs)).toString();
    }
}
