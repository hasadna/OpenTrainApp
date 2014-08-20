package il.org.hasadna.opentrain.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import il.org.hasadna.opentrain.application.Logger;
import il.org.hasadna.opentrain.application.SharedConstants;
import il.org.hasadna.opentrain.application.preferences.Prefs;

public class WifiScanner extends BroadcastReceiver {
    public static final String ACTION_WIFIS_SCANNED = SharedConstants.ACTION_NAMESPACE + ".WifiScanner.WifisScanned";
    public static final String WIFI_SCANNER_ARG_SCANRESULT = SharedConstants.NAMESPACE + ".WifiScanner.ScanResult";
    public static final String ACTION_WIFIS_SCANNING_MODE_CHANGED = SharedConstants.ACTION_NAMESPACE + ".WifiScanner.WifisScanningModeChanged";
    public static final String WIFI_SCANNER_ARG_MODE = SharedConstants.NAMESPACE + ".WifiScanner.Mode";
    public static final int MODE_TRAIN_WIFI_SCANNING = 1;
    private int mode = MODE_TRAIN_WIFI_SCANNING;
    public static final int MODE_TRAIN_WIFI_FOUND = 2;
    private static final String LOGTAG = WifiScanner.class.getName();
    private final Context mContext;
    private GPSScanner gpsScanner;
    private boolean mStarted;
    private WifiLock mWifiLock;
    private Timer mWifiScanTimer;
    private Prefs mPrefs;
    private long mLastUpdateTime;
    private long mLastTrainIndicationTime;
    private String mStationName;

    public WifiScanner(Context context) {
        mContext = context;
        mPrefs = Prefs.getInstance(context);
    }

    private static boolean shouldLog(ScanResult scanResult) {
        if (WifiMacCanonicalizer.contains(scanResult)) {
            Log.d(LOGTAG, "Blocked BSSID: " + scanResult);
            return false;
        }
        if (WifiNameFilter.contains(scanResult)) {
            Log.d(LOGTAG, "Blocked SSID: " + scanResult);
            return false;
        }
        return true;
    }

    private static boolean isTrainIndication(ScanResult scanResult) {
        if (WifiNameFilter.trainIndicatorsContain(scanResult)) {
            Log.i(LOGTAG, "Train SSID: " + scanResult);
            return true;
        }
        return false;
    }

    public void start() {
        if (mStarted) {
            return;
        }
        mStarted = true;

        WifiManager wm = getWifiManager();
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, LOGTAG);
        mWifiLock.acquire();

        IntentFilter i = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(this, i);

        // Ensure that we are constantly scanning for new access points.
        setMode(MODE_TRAIN_WIFI_SCANNING);
    }

    public void stop() {
        if (mWifiLock != null) {
            mWifiLock.release();
            mWifiLock = null;
        }

        if (mWifiScanTimer != null) {
            mWifiScanTimer.cancel();
            mWifiScanTimer = null;
        }
        if (mStarted) {
            mContext.unregisterReceiver(this);
        }
        mStarted = false;
    }

    @Override
    public void onReceive(Context c, Intent intent) {

        JSONArray wifiInfo = new JSONArray();
        boolean isTrainIndication = false;

        Collection<ScanResult> scanResults = getWifiManager().getScanResults();
        long timestamp = System.currentTimeMillis();
        for (ScanResult scanResult : scanResults) {
            scanResult.BSSID = WifiMacCanonicalizer.canonicalizeBSSID(scanResult.BSSID);
            if (!shouldLog(scanResult)) {
                continue;
            }
            if (isTrainIndication(scanResult)) {
                isTrainIndication = true;
                if ("S-ISRAEL-RAILWAYS".equals(scanResult.SSID)) {
                    String stationName = BssidUtils.getName(scanResult.BSSID);
                    if (!stationName.equals(mStationName)) {
                        mStationName = stationName;
                    }
                }
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("SSID", scanResult.SSID);
                    obj.put("key", scanResult.BSSID);
                    obj.put("frequency", scanResult.frequency);
                    obj.put("signal", scanResult.level);
                    obj.put("timestamp", timestamp);
                    wifiInfo.put(obj);
                } catch (JSONException jsonex) {

                }
            }
        }

        reportWifi(wifiInfo, isTrainIndication);
    }

    public void reportWifi(JSONArray wifiInfo, boolean isTrainIndication) {
        if (isTrainIndication) {
            mLastTrainIndicationTime = System.currentTimeMillis();
        }

        checkForStateChange(isTrainIndication);

        if (wifiInfo.length() == 0) {
            return;
        }

        if (Logger.logFlag) {
            Logger.wifi(wifiInfo.toString());
        }

        long currentTime = System.currentTimeMillis();
        long timeDelta = currentTime - mLastUpdateTime;
        if (timeDelta > mPrefs.WIFI_MIN_UPDATE_TIME) {
            mLastUpdateTime = currentTime;
            reportScanResults(wifiInfo, isTrainIndication);
        }
    }

    private WifiManager getWifiManager() {
        return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    private void setMode(int mode) {
        this.mode = mode;
        if (MODE_TRAIN_WIFI_SCANNING == mode) {
            schedulemWifiScanTimer(mPrefs.WIFI_MODE_TRAIN_SCANNIG_PERIOD);
            if (gpsScanner != null)
                gpsScanner.stop();
        } else if (MODE_TRAIN_WIFI_FOUND == mode) {
            schedulemWifiScanTimer(mPrefs.WIFI_MODE_TRAIN_FOUND_PERIOD);
            if (gpsScanner != null)
                gpsScanner.start();
        }
    }

    private void schedulemWifiScanTimer(long period) {
        if (mWifiScanTimer != null) {
            mWifiScanTimer.cancel();
        }
        mWifiScanTimer = new Timer();
        mWifiScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(LOGTAG, "WiFi Scanning Timer fired");
                WifiManager wm = getWifiManager();
                boolean enable = wm.isWifiEnabled();
                if (!enable) {
                    wm.setWifiEnabled(true);
                }
                getWifiManager().startScan();
            }
        }, 0, period);
    }

    private void checkForStateChange(boolean isTrainIndication) {
        int newMode = isTrainIndication ? MODE_TRAIN_WIFI_FOUND
                : MODE_TRAIN_WIFI_SCANNING;
        if (mode != newMode) {
            setMode(newMode);
            broadcastModeChanged();
            if (Logger.logFlag) {
                Logger.wifi("mode changed: " + mode);
            }
        }
    }

    public void setGPSScanner(GPSScanner gpsScanner) {
        this.gpsScanner = gpsScanner;
    }

    private void reportScanResults(JSONArray wifiInfo, boolean isTrainIndication) {
        Intent i = new Intent(WifiScanner.ACTION_WIFIS_SCANNED);
        i.putExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT, wifiInfo.toString());
        i.putExtra(SharedConstants.NAME_TIME, System.currentTimeMillis());
        i.putExtra("TrainIndication", isTrainIndication);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void broadcastModeChanged() {
        Intent i = new Intent(ACTION_WIFIS_SCANNING_MODE_CHANGED);
        i.putExtra(WIFI_SCANNER_ARG_MODE, mode);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    public long getmLastTrainIndicationTime() {
        return mLastTrainIndicationTime;
    }

    public String getmStationName() {
        return mStationName != null ? mStationName : "";
    }
}
