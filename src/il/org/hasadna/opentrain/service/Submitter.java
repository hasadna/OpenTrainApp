package il.org.hasadna.opentrain.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import il.org.hasadna.opentrain.application.Logger;
import il.org.hasadna.opentrain.application.preferences.Prefs;
import il.org.hasadna.opentrain.client.activity.MainActivity;

/**
 * Created by noam on 08/06/2014.
 */
public class Submitter {

    private JSONArray mReports = new JSONArray();
    private final Context mContext;
    private final Prefs mPrefs;
    private static Submitter mInstance;

    private static final String LOCATION_URL = "http://opentrain.hasadna.org.il/reports/add/";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private String USER_AGENT_STRING;
    private static final String DEVICE_ID_HEADER = "device_id";
    private static final String TIMESTAMP_HEADER = "timestamp";
    private URL mURL;
    private int mReportsSent = 0;
    public long mLastUploadTime = 0;

    private Submitter(Context context) {
        mContext = context;
        mPrefs = Prefs.getInstance(context);

        String apiKey = PackageUtils.getMetaDataString(context, "il.org.hasadna.opentrain.API_KEY");
        try {
            mURL = new URL(LOCATION_URL + "?key=" + apiKey);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        USER_AGENT_STRING = NetworkUtils.getUserAgentString(mContext);
    }

    public static Submitter getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Submitter.class) {
                if (mInstance == null) {
                    mInstance = new Submitter(context);
                }
            }
        }
        return mInstance;
    }

    public void submit(JSONObject jsonObject) {
        if (Logger.logFlag) {
            Logger.submitter(jsonObject.toString());
        }
        mReports.put(jsonObject);
        int count = mReports.length();
        if (count == 0) {
            return;
        }

        if (count < mPrefs.RECORD_BATCH_SIZE) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            return;
        }

        JSONArray reports = mReports;
        uploadReports(reports);
        mReports = new JSONArray();
    }

    private void uploadReports(final JSONArray reports) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceId = mPrefs.getDailyID();
                String timestamp = Long.toString(System.currentTimeMillis());

                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) mURL
                            .openConnection();

                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty(USER_AGENT_HEADER,
                            USER_AGENT_STRING);

                    urlConnection.setRequestProperty(DEVICE_ID_HEADER, deviceId);

                    urlConnection.setRequestProperty(TIMESTAMP_HEADER, timestamp);
                    JSONObject wrapper = new JSONObject();
                    wrapper.put("items", reports);
                    String wrapperData = wrapper.toString();
                    byte[] bytes = wrapperData.getBytes();
                    urlConnection.setFixedLengthStreamingMode(bytes.length);
                    OutputStream out = new BufferedOutputStream(
                            urlConnection.getOutputStream());
                    out.write(bytes);
                    out.flush();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode >= 200 && responseCode <= 299) {
                        mReportsSent = mReportsSent + reports.length();
                    }

                    InputStream in = new BufferedInputStream(
                            urlConnection.getInputStream());
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder(in.available());
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }
                    r.close();
                    DataManager.getInstance().getTripInfo(total.toString());
                    mLastUploadTime = System.currentTimeMillis();
                    broadcastReportsStats();
                } catch (Exception ex) {
                    Log.d("", ex.toString());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }

                }
            }
        }).start();
    }

    private void broadcastReportsStats() {
        Intent i = new Intent(MainActivity.ACTION_UPDATE_UI);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    public int reportSent() {
        return mReportsSent;
    }

    public int reportsPending() {
        return mReports.length();
    }

    public long lastReport() {
        return mLastUploadTime;
    }
}
