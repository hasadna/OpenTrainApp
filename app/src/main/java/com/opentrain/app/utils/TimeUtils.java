package com.opentrain.app.utils;

import java.util.Date;

/**
 * Created by noam on 07/06/15.
 */
public class TimeUtils {

    public static String getFormattedTime() {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getFormattedTime(long unixTimeMs) {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new Date(unixTimeMs));
    }
}
