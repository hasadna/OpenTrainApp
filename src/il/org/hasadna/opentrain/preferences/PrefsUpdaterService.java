package il.org.hasadna.opentrain.preferences;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import il.org.hasadna.opentrain.ScannerService;

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
            String updateRefs = getPrefrenceFromServer();
            Prefs prefs = Prefs.getInstance(this);
            prefs.setPreferenceFromServer(updateRefs);
            prefs.savePreferenceFromServer();

            //if service running
            Intent i = new Intent(ScannerService.ACTION_PREFS_UPDATED_FROM_SERVER);
            i.putExtra(Intent.EXTRA_SUBJECT, updateRefs);
            sendBroadcast(i);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String getPrefrenceFromServer() {

        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(PrefsUpdater.URL_CONFIG);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            String result = getStringFromInputStream(in);
            return result;
        } catch (Exception e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "error";
    }

    private static String getStringFromInputStream(InputStream inputStream)
            throws Exception {
        BufferedReader r = new BufferedReader(
                new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }
}
