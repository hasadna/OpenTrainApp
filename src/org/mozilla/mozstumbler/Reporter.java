package org.mozilla.mozstumbler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.mozilla.mozstumbler.preferences.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
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

class Reporter extends BroadcastReceiver {
    private static final String LOGTAG          = Reporter.class.getName(); 
    private static final String LOCATION_URL    = "http://54.221.246.54/reports/add/";//"https://location.services.mozilla.com/v1/submit"; //TODO: hasadna this should contain our own url
    private static final String NICKNAME_HEADER = "X-Nickname";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final int RECORD_BATCH_SIZE  = 20;
    private static final long TRAIN_INDICATION_TTL = 1 * 60 * 60 * 1000;

    private static String       MOZSTUMBLER_USER_AGENT_STRING;
    private static String       MOZSTUMBLER_API_KEY_STRING;

    private final Context       mContext;
    private final Prefs         mPrefs;
    private JSONArray           mReports;
    private long                mLastUploadTime;
    private URL                 mURL; 

    private long mReportsSent;
    
    private long mLastTrainIndicationTime;

    Reporter(Context context, Prefs prefs) {
    	
        mContext = context;
        mPrefs = prefs;
        mLastTrainIndicationTime = 0;
        
        MOZSTUMBLER_USER_AGENT_STRING = NetworkUtils.getUserAgentString(mContext);

        String storedReports = mPrefs.getReports();
        try {
            mReports = new JSONArray(storedReports);
        } catch (Exception e) {
            mReports = new JSONArray();
        }

        String apiKey = PackageUtils.getMetaDataString(context, "org.mozilla.mozstumbler.API_KEY");
        try {
            mURL = new URL(LOCATION_URL + "?key=" + apiKey);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        mContext.registerReceiver(this, new IntentFilter(ScannerService.MESSAGE_TOPIC));
    }

    void shutdown() {
        Log.d(LOGTAG, "shutdown");

        // Attempt to write out mReports
        mPrefs.setReports(mReports.toString());
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (!action.equals(ScannerService.MESSAGE_TOPIC)) {
            Log.e(LOGTAG, "Received an unknown intent");
            return;
        }
        // We should only consider reporting if we are in a train context
        long time = intent.getLongExtra("time", 0);
        boolean isTrainIndication = intent.getBooleanExtra("TrainIndication", false);
        if (isTrainIndication) {
        	mLastTrainIndicationTime = time;
        }
        
        if (System.currentTimeMillis() - mLastTrainIndicationTime > TRAIN_INDICATION_TTL) {
        	// We are not in train context. Don't report.
        	return;
        }
        
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String data = intent.getStringExtra("data");
        
        if (data!=null) Log.d(LOGTAG, "" + subject + " : " + data);

        String wifiData = "";
        String cellData = "";
        String radioType = "";
        String GPSData = "";
        if (subject.equals("WifiScanner")) {
        	wifiData = data;
            Log.d(LOGTAG, "Reporter data: WiFi: "+wifiData.length());
        } else if (subject.equals("CellScanner")) {
        	cellData = data;
        	radioType = intent.getStringExtra("radioType");
            Log.d(LOGTAG, "Reporter data: Cell: "+cellData.length()+" ("+radioType+")");
        } else if (subject.equals("GPSScanner")) {
        	GPSData = data;
            Log.d(LOGTAG, "Reporter data: GPS: "+GPSData.length());
        }
        else {
            Log.d(LOGTAG, "Intent ignored with Subject: " + subject);
            return; // Intent not aimed at the Reporter (it is possibly for UI instead)
        }
        
        // HASADNA: removed the following condition because we want to send data even when no gps is available.
        //if (mGPSData.length() > 0 && (mWifiData.length() > 0 || mCellData.length() > 0)) {
        reportLocation(time, GPSData, wifiData, radioType, cellData);
        //}
    }

    void sendReports(boolean force) {
        Log.d(LOGTAG, "sendReports: force=" + force);

        int count = mReports.length();
        if (count == 0) {
            Log.d(LOGTAG, "no reports to send");
            return;
        }

        if (count < RECORD_BATCH_SIZE && !force && mLastUploadTime > 0) {
            Log.d(LOGTAG, "batch count not reached, and !force");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            Log.d(LOGTAG, "Can't send reports without network connection");
            return;
        }

        JSONArray reports = mReports;
        mReports = new JSONArray();

        String nickname = mPrefs.getNickname();
        
        spawnReporterThread(reports, nickname);
    }

    private void spawnReporterThread(final JSONArray reports, final String nickname) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.d(LOGTAG, "sending results...");
                    
                    HttpURLConnection urlConnection = (HttpURLConnection) mURL.openConnection();
                    try {
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty(USER_AGENT_HEADER, MOZSTUMBLER_USER_AGENT_STRING);

                        if (nickname != null) {
                            urlConnection.setRequestProperty(NICKNAME_HEADER, nickname);
                        }

                        JSONObject wrapper = new JSONObject();
                        wrapper.put("items", reports);
                        String wrapperData = wrapper.toString();
                        byte[] bytes = wrapperData.getBytes();
                        urlConnection.setFixedLengthStreamingMode(bytes.length);
                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                        out.write(bytes);
                        out.flush();

                        Log.d(LOGTAG, "uploaded wrapperData: " + wrapperData + " to " + mURL.toString());

                        int code = urlConnection.getResponseCode();
                        if (code >= 200 && code <= 299) {
                            mReportsSent = mReportsSent + reports.length();
                        }
                        Log.e(LOGTAG, "urlConnection returned " + code);

                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder(in.available());
                        String line;
                        while ((line = r.readLine()) != null) {
                          total.append(line);
                        }
                        r.close();

                        mLastUploadTime = System.currentTimeMillis();
                        sendUpdateIntent();

                        Log.d(LOGTAG, "response was: \n" + total + "\n");
                    } catch (JSONException jsonex) {
                        Log.e(LOGTAG, "error wrapping data as a batch", jsonex);
                    } catch (Exception ex) {
                        Log.e(LOGTAG, "error submitting data", ex);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception ex) {
                    Log.e(LOGTAG, "error submitting data", ex);
                }
            }
        }).start();
    }

    void reportLocation(long time, String location, String wifiInfo, String radioType, String cellInfo) {
        // HASADNA: added this to enable debugging:
        android.os.Debug.waitForDebugger();
    	Log.d(LOGTAG, "reportLocation called");
        JSONObject locInfo = null;
        JSONArray cellJSON = null
                ,wifiJSON = null;

        try {
        	if (location.length()>0) {
                locInfo = new JSONObject( location );
        	} else { 
        		locInfo = new JSONObject( );
            }
        	
            locInfo.put("time", time);
            
            if (cellInfo.length()>0) {
                cellJSON=new JSONArray(cellInfo);
                locInfo.put("cell", cellJSON);
                locInfo.put("radio", radioType);
            }
            
            if (wifiInfo.length()>0) {
                wifiJSON=new JSONArray(wifiInfo);
                locInfo.put("wifi", wifiJSON);
            }
            
            // At least one cell or wifi entry is required
            // as per: https://mozilla-ichnaea.readthedocs.org/en/latest/api/submit.html
            if (cellJSON == null && wifiJSON == null) {
                Log.w(LOGTAG, "Invalid report: at least one cell/wifi entry is required");
                return;
            }

        } catch (JSONException jsonex) {
            Log.w(LOGTAG, "JSON exception", jsonex);
            return;
        }

        mReports.put(locInfo);
        sendReports(false);
    }

    public long getLastUploadTime() {
        return mLastUploadTime;
    }

    public long getReportsSent() {
        return mReportsSent;
    }
    
    public long getLastTrainIndicationTime() {
        return mLastTrainIndicationTime;
    }

    private void sendUpdateIntent() {
        Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
        i.putExtra(Intent.EXTRA_SUBJECT, "Reporter");
        mContext.sendBroadcast(i);
    }
}
