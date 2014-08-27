package il.org.hasadna.opentrain.service;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateTimeUtils {
    private static final DateFormat mISO8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    private static final DateFormat uiFormat = new SimpleDateFormat("HH:mm");

    static final long MILLISECONDS_PER_DAY = 86400000;  // milliseconds/day

    static {
        mISO8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private DateTimeUtils() {
    }

    @SuppressLint("SimpleDateFormat")
    static String formatDate(Date date) {
        return mISO8601Format.format(date);
    }

    static String formatTime(long time) {
        return formatDate(new Date(time));
    }

    public static String formatTimeForLocale(long time) {
        return uiFormat.format(time);
    }
}
