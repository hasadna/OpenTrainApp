package il.org.hasadna.opentrain.preferences;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.network.RequestManager;

/**
 * Created by Noam.m on 3/23/2014.
 */
public class PrefsUpdaterService extends IntentService {

    public static final int SERVICE_ID = 1234;
    private static final String TAG = PrefsUpdaterService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PrefsUpdaterService() {
        super("PrefsUpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String updateRefs = RequestManager.getPrefrenceFromServer();
            Prefs.getInstance(this).setPreferenceFromServer(updateRefs);
            PrefsUpdaterService.notifyBar(this, updateRefs);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void notifyBar(Context context, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);
        Notification notif = new Notification(R.drawable.ic_launcher,
                "Crazy About Android...", System.currentTimeMillis());
        notif.setLatestEventInfo(context, currentDateandTime, message, contentIntent);
        nm.notify(1, notif);
    }
}
