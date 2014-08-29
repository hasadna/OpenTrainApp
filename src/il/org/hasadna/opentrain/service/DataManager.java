package il.org.hasadna.opentrain.service;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by android on 29/08/2014.
 */
public class DataManager {

    private static final long BSSID_TIME_STAMP = 1000 * 60 * 60;
    private static DataManager mInstance;
    private HashMap<String, String> bssids = new HashMap<String, String>();
    private long bssidsStamp;
    private boolean bssidTaskFlag;

    public static DataManager getInstance() {
        if (mInstance == null) {
            synchronized (DataManager.class) {
                if (mInstance == null) {
                    mInstance = new DataManager();
                }
            }
        }
        return mInstance;
    }

    public String getName(String bssid) {
        if (System.currentTimeMillis() - bssidsStamp > BSSID_TIME_STAMP) {
            if (!bssidTaskFlag) {
                bssidTaskFlag = true;
                new GetBSSIDContent().execute();
            }
            return "";
        } else {
            String value = bssids.get(bssid);
            return null != value ? value : "";
        }
    }

    class GetBSSIDContent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLsUtils.URLStops);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                JSONObject jsonObject = new JSONObject(total.toString());
                Iterator<String> iter = jsonObject.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        JSONObject value = (JSONObject) jsonObject.get(key);
                        String stop_name = value.getString("stop_name");
                        bssids.put(key, stop_name);
                    } catch (JSONException e) {
                        // Something went wrong!
                    }
                }
                bssidsStamp = System.currentTimeMillis();
            } catch (Exception e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            bssidTaskFlag = false;
            return null;
        }
    }
}
