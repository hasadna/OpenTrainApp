package il.org.hasadna.opentrain;

import android.util.Log;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Environment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.org.hasadna.opentrain.monitoring.JsonDumper;
import il.org.hasadna.opentrain.preferences.Prefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class WifiScanner extends BroadcastReceiver {
  private static final String LOGTAG              = WifiScanner.class.getName();
  
  public static final String WIFI_SCANNER_EXTRA_SUBJECT = "WifiScanner";
  public static final String WIFI_SCANNER_ARG_SCANRESULT = "il.org.hasadna.opentrain.WifiScanner.ScanResult";

 
  private static final int MODE_TRAIN_WIFI_SCANNING = 1;
  private static final int MODE_TRAIN_WIFI_FOUND = 2;
  private int mode = MODE_TRAIN_WIFI_SCANNING;
  
  private LocationScanner locationScanner;
  
  private boolean                mStarted;
  private final Context          mContext;
  private WifiLock               mWifiLock;
  private Timer                  mWifiScanTimer;
  private final Set<String>      mAPs = new HashSet<String>();
  private long                mLastUpdateTime;
  
  private JsonDumper		mJsonDumperWifi;

  private Prefs mPrefs;
  
  WifiScanner(Context context) {
    mContext = context;
    mPrefs = Prefs.getInstance(context);
    mStarted = false;
    mJsonDumperWifi= new JsonDumper(context,"raw.wifi");
  }

  public void start() {
    if (mStarted) {
        return;
    }
    mStarted = true;
  
    mJsonDumperWifi.open();

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

    mContext.unregisterReceiver(this);
    
    mJsonDumperWifi.close();

    mStarted = false;
  }

  @Override
public void onReceive(Context c, Intent intent) {
		Log.d(LOGTAG,"onReceive:");

    Collection<ScanResult> scanResults = getWifiManager().getScanResults();
    
   mJsonDumperWifi.dump(scanResults);//Note that all scan results are logged to file, whether rail indicators or not.
    
    boolean isTrainIndication = false;
    JSONArray wifiInfo = new JSONArray();
    ArrayList<ScanResult> railWifiScanResults= new ArrayList<ScanResult>();
    
    for (ScanResult scanResult : scanResults) {
      scanResult.BSSID = WifiMacCanonicalizer.canonicalizeBSSID(scanResult.BSSID);
      if (!shouldLog(scanResult)) {
        continue;
      }
      if (isTrainIndication(scanResult)) {
    	  isTrainIndication = true;
    	  railWifiScanResults.add(scanResult);//EyalLiebermann TODO: Keep this and remove usage of string and rely on Parcelable ArrayList after dumps prove its reliable

    	  try {
    		  JSONObject obj = new JSONObject();//EyalLiebermann TODO: Remove usage of json string and rely on Parcelable ArrayList after dumps prove its reliable
    		  obj.put("SSID", scanResult.SSID);
    		  obj.put("key", scanResult.BSSID);
    		  obj.put("frequency", scanResult.frequency);
    		  obj.put("signal", scanResult.level);    		  
    		  wifiInfo.put(obj);
    	  } catch (JSONException jsonex) {
    		  Log.e(LOGTAG, "", jsonex);
    	  }

    	  mAPs.add(scanResult.BSSID);

    	  Log.v(LOGTAG, "BSSID=" + scanResult.BSSID + ", SSID=\"" + scanResult.SSID + "\", Signal=" + scanResult.level);
      }
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
	    i.putExtra(Intent.EXTRA_SUBJECT, WIFI_SCANNER_EXTRA_SUBJECT);
	    i.putExtra("data", wifiInfo.toString());//EyalLiebermann TODO: Remove usage of string and rely on Parcelable ArrayList after dumps prove its reliable
	    i.putExtra("time", currentTime);
	    i.putParcelableArrayListExtra(WIFI_SCANNER_ARG_SCANRESULT, railWifiScanResults);//EyalLiebermann TODO: Keep this one. Remove usage of json string and rely on Parcelable ArrayList after dumps prove its reliable
	    if (isTrainIndication) {//EyalLiebermann. Check seems redundant. if no train indication then wifi json array si proabably empty and we do not get to this if statement. Therefore parameter itself is redundant as well. 
	        i.putExtra("TrainIndication", true);
	    }
	    mContext.sendBroadcast(i);
    }
  }

  public int getAPCount() {
    return mAPs.size();
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

  private WifiManager getWifiManager() {
    return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
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
		}
	}

	public void setLocationScanner(LocationScanner locationScanner) {
		this.locationScanner=locationScanner;
	}
	
}
