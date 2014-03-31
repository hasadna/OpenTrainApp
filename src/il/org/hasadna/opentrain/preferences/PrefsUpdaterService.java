package il.org.hasadna.opentrain.preferences;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
            Prefs prefs = Prefs.getInstance(this);
            prefs.setPreferenceFromServer(updateRefs);
            prefs.savePreferenceFromServer();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
