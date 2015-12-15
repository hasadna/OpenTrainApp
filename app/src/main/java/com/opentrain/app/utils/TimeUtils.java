package com.opentrain.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    // Into unix milliseconds - here we get only the time and add the date today
    public static long getFormattedTime(String dateString) throws ParseException {
        String fullDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " " + dateString;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.parse(fullDate).getTime();
    }
}
