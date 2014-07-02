package il.org.hasadna.opentrain.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import il.org.hasadna.opentrain.application.preferences.Prefs;
import il.org.hasadna.opentrain.client.activity.MainActivity;

public class Reporter extends BroadcastReceiver {
    private static final String LOGTAG = Reporter.class.getName();

    private final Context mContext;
    private final Prefs mPrefs;

    private Location mGpsPosition;
    private final ArrayList<JSONObject> mWifiData = new ArrayList<JSONObject>();

    private long mLastTrainIndicationTime;

    public Reporter(Context context) {
        mContext = context;
        mPrefs = Prefs.getInstance(context);
        mLastTrainIndicationTime = 0;
        resetData();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiScanner.ACTION_WIFIS_SCANNED);
        intentFilter.addAction(GPSScanner.ACTION_GPS_UPDATED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this,
                intentFilter);
    }

    private void resetData() {
        mWifiData.clear();
        mGpsPosition = null;
    }

    void flush() {
        reportCollectedLocation();
    }

    public void shutdown() {
        flush();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        long time = intent.getLongExtra("time", 0);
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        // We should only consider reporting if we are in a train context
        boolean isTrainIndication = intent.getBooleanExtra("TrainIndication", false);
        if (isTrainIndication) {
            mLastTrainIndicationTime = time;
            broadcastTrainIndicationStats();
        }
        if (System.currentTimeMillis() - mLastTrainIndicationTime > mPrefs.TRAIN_INDICATION_TTL) {
            // We are not in train context. Don't report.
            return;
        }

        if (action.equals(WifiScanner.ACTION_WIFIS_SCANNED)) {
            String wifiInfoString = intent.getStringExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT);
            putWifiResults(wifiInfoString);
        } else if (action.equals(GPSScanner.ACTION_GPS_UPDATED)) {
            mGpsPosition = intent.getParcelableExtra(GPSScanner.GPS_SCANNER_ARG_LOCATION);
            return;
        } else {
            Log.d(LOGTAG, "Intent ignored with Subject: " + subject);
            return; // Intent not aimed at the Reporter (it is possibly for UI instead)
        }

        //we want to send data even when no gps is available
        reportCollectedLocation();
    }

    private void putWifiResults(String wifiInfoString) {
        try {
            JSONArray jsonArray = new JSONArray(wifiInfoString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                mWifiData.add(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void reportCollectedLocation() {
        try {
            if (mWifiData.isEmpty()) {
                return;
            }
            JSONObject report = new JSONObject();

            //wifi data
            JSONArray wifiJSON = new JSONArray();
            for (JSONObject jsonObject : mWifiData) {
                wifiJSON.put(jsonObject);
            }
            mWifiData.clear();
            report.put("wifi", wifiJSON);

            //location data
            if (mGpsPosition != null) {
                JSONObject locAPIInfo = new JSONObject();
                locAPIInfo.put("time", mGpsPosition.getTime());
                locAPIInfo.put("long", mGpsPosition.getLongitude());
                locAPIInfo.put("lat", mGpsPosition.getLatitude());
                locAPIInfo.put("provider", mGpsPosition.getProvider());
                locAPIInfo.put("accuracy", mGpsPosition.hasAccuracy() ? mGpsPosition.getAccuracy() : null);
                locAPIInfo.put("altitude", mGpsPosition.hasAltitude() ? mGpsPosition.getAltitude() : null);
                locAPIInfo.put("bearing", mGpsPosition.hasBearing() ? mGpsPosition.getBearing() : null);
                locAPIInfo.put("speed", mGpsPosition.hasSpeed() ? mGpsPosition.getSpeed() : null);
                report.put("location_api", locAPIInfo);
            } else {
                report.put("location_api", null);
            }

            //configuration data
            String hashed_device_id = mPrefs.getDailyID();
            report.put("device_id", hashed_device_id);
            report.put("app_version_code", mPrefs.VERSION_CODE);
            report.put("app_version_name", mPrefs.VERSION_NAME);
            report.put("config_version", mPrefs.CONFIG_VERSION);
            report.put("time", System.currentTimeMillis());

            Submitter.getInstance(mContext).submit(report);
            broadcastReportsPendingStats();
        } catch (Exception e) {

        }
    }

    private void broadcastTrainIndicationStats() {
        Intent i = new Intent(MainActivity.ACTION_UPDATE_UI);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void broadcastReportsPendingStats() {
        Intent i = new Intent(MainActivity.ACTION_UPDATE_UI);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

}
