package il.org.hasadna.opentrain.preferences;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Noam.m on 3/13/14.
 */
public class PrefsUpdater {

    public static final String URL_CONFIG = "http://opentrain.hasadna.org.il/client/config";
    private static long PREFS_UPDATE_INTERVAL = 24 * 60 * 60 * 1000;
    public static final String ACTION_PREFS_UPDATED_FROM_SERVER = "il.org.hasadna.opentrain.serviceMessage.prefsupdated";

    public static void scheduleUpdate(Activity context) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Random random = new Random();
        int randomMinute = random.nextInt(60);

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 5);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.SECOND, randomMinute);

        Intent intent = new Intent(context, PrefsUpdaterService.class);
        PendingIntent pintent = PendingIntent.getService(context, PrefsUpdaterService.SERVICE_ID, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PREFS_UPDATE_INTERVAL, pintent);
    }
}
