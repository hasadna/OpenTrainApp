package test;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import il.org.hasadna.opentrain.Scanner;
import il.org.hasadna.opentrain.ScannerService;
import il.org.hasadna.opentrain.WifiScanner;
import il.org.hasadna.opentrain.preferences.Prefs;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockWifiScanner implements Scanner.IScanner {

    private static final int MODE_TRAIN_WIFI_SCANNING = 1;
    private static final int MODE_TRAIN_WIFI_FOUND = 2;
    private int mode = MODE_TRAIN_WIFI_SCANNING;

    private Scanner.IScanner locationScanner;
    private boolean                mStarted;
    private final Context          mContext;
    private Timer                  mWifiScanTimer;
    private Prefs mPrefs;
    private long                mLastUpdateTime;

    public MockWifiScanner(Context context) {
        mContext = context;
        mPrefs = Prefs.getInstance(context);
        mStarted = false;
    }

    @Override
    public void start() {
        if (mStarted) {
            return;
        }
        mStarted = true;
        setMode(MODE_TRAIN_WIFI_SCANNING);
    }

    @Override
    public void stop() {
        if (mWifiScanTimer != null) {
            mWifiScanTimer.cancel();
            mWifiScanTimer = null;
        }
        mStarted = false;
    }

    private void setMode(int mode) {
        this.mode = mode;
        if (MODE_TRAIN_WIFI_SCANNING == mode) {
            schedulemWifiScanTimer(mPrefs.WIFI_MODE_TRAIN_SCANNIG_PERIOD);
            if (locationScanner != null)
                locationScanner.stop();
        } else if (MODE_TRAIN_WIFI_FOUND == mode) {
            schedulemWifiScanTimer(mPrefs.WIFI_MODE_TRAIN_FOUND_PERIOD);
            if (locationScanner != null)
                locationScanner.start();
        }
    }

    private void schedulemWifiScanTimer(long period) {
        if(mWifiScanTimer!=null){
            mWifiScanTimer.cancel();
        }
        mWifiScanTimer = new Timer();
        mWifiScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onReceive();
            }
        }, 0, period);
    }

    private void checkForStateChange(boolean isTrainIndication) {
        int newMode = isTrainIndication ? MODE_TRAIN_WIFI_FOUND
                : MODE_TRAIN_WIFI_SCANNING;
        if (mode != newMode) {
            setMode(newMode);
        }
    }

    public void setLocationScanner(Scanner.IScanner locationScanner) {
        this.locationScanner=locationScanner;
    }

    public void onReceive() {

        boolean isTrainIndication = false;
        JSONArray wifiInfo = new JSONArray();
        ArrayList<ScanResult> railWifiScanResults= new ArrayList<ScanResult>();

        isTrainIndication = true;


        try {
            JSONObject obj = new JSONObject();
            obj.put("SSID", "ISRAEL-RAILWAYS");
            obj.put("key", "34:08:04:73:6c:56");
            obj.put("frequency", "2437");
            obj.put("signal", "-93");
            wifiInfo.put(obj);
        } catch (JSONException jsonex) {

        }

        checkForStateChange(isTrainIndication);

        // No scan results to report.
        if (wifiInfo.length() == 0) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long timeDelta = currentTime - mLastUpdateTime;
        if (timeDelta > mPrefs.WIFI_MIN_UPDATE_TIME) {
            mLastUpdateTime = currentTime;
            Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
            i.putExtra(Intent.EXTRA_SUBJECT, WifiScanner.WIFI_SCANNER_EXTRA_SUBJECT);
            i.putExtra("data", wifiInfo.toString());
            i.putExtra("time", currentTime);
            i.putParcelableArrayListExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT, railWifiScanResults);
            if (isTrainIndication) {
                i.putExtra("TrainIndication", true);
            }
            mContext.sendBroadcast(i);
        }
    }
}
