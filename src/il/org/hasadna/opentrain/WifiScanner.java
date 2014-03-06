package il.org.hasadna.opentrain;

import android.util.Log;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class WifiScanner extends BroadcastReceiver {
  private static final String LOGTAG              = WifiScanner.class.getName();
  private static final long WIFI_MIN_UPDATE_TIME  = 1000; // milliseconds, update at least every WIFI_MIN_UPDATE_TIME milliseconds
  private static final long WIFI_MAX_UPDATE_PERIOD = WIFI_MIN_UPDATE_TIME; // milliseconds, update at most every WIFI_MAX_UPDATE_PERIOD milliseconds

  private static final int MODE_TRAIN_WIFI_SCANNIG = 1;
  private static final int MODE_TRAIN_WIFI_FOUND = 2;
  private int mode = MODE_TRAIN_WIFI_SCANNIG;
  private static final int MODE_TRAIN_WIFI_FOUND_PERIOD = 1*15*1000;
  private static final int MODE_TRAIN_WIFI_SCANNIG_PERIOD = 5*60*1000;
  private LocationScanner locationScanner;
  
  private boolean                mStarted;
  private final Context          mContext;
  private WifiLock               mWifiLock;
  private Timer                  mWifiScanTimer;
  private final Set<String>      mAPs = new HashSet<String>();
  private long                mLastUpdateTime;

  WifiScanner(Context c) {
    mContext = c;
    mStarted = false;
  }

  public void start() {
    if (mStarted) {
        return;
    }
    mStarted = true;

    WifiManager wm = getWifiManager();
    mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,
                                  "MozStumbler");
    mWifiLock.acquire();

    IntentFilter i = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    mContext.registerReceiver(this, i);

    // Ensure that we are constantly scanning for new access points.
    setMode(MODE_TRAIN_WIFI_SCANNIG);
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
    mStarted = false;
  }

  @Override
public void onReceive(Context c, Intent intent) {
    Collection<ScanResult> scanResults = getWifiManager().getScanResults();
    boolean isTrainIndication = false;
    JSONArray wifiInfo = new JSONArray();
    for (ScanResult scanResult : scanResults) {
      scanResult.BSSID = BSSIDBlockList.canonicalizeBSSID(scanResult.BSSID);
      if (!shouldLog(scanResult)) {
        continue;
      }
      if (isTrainIndication(scanResult)) {
    	  isTrainIndication = true;

    	  try {
    		  JSONObject obj = new JSONObject();
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
    if (timeDelta > WifiScanner.WIFI_MAX_UPDATE_PERIOD) {
    	mLastUpdateTime = currentTime; 
	    Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
	    i.putExtra(Intent.EXTRA_SUBJECT, "WifiScanner");
	    i.putExtra("data", wifiInfo.toString());
	    i.putExtra("time", currentTime);
	    if (isTrainIndication) {
	        i.putExtra("TrainIndication", true);
	    }
	    mContext.sendBroadcast(i);
    }
  }

  public int getAPCount() {
    return mAPs.size();
  }

  private static boolean shouldLog(ScanResult scanResult) {
    if (BSSIDBlockList.contains(scanResult)) {
      Log.w(LOGTAG, "Blocked BSSID: " + scanResult);
      return false;
    }
    if (SSIDBlockList.contains(scanResult)) {
      Log.w(LOGTAG, "Blocked SSID: " + scanResult);
      return false;
    }
    return true;
  }
  
	private static boolean isTrainIndication(ScanResult scanResult) {
		if (SSIDBlockList.trainIndicatorsContain(scanResult)) {
			Log.w(LOGTAG, "Train SSID: " + scanResult);
			return true;
		}
		return false;
	}

  private WifiManager getWifiManager() {
    return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
  }
  
  private void setMode(int mode) {
		this.mode = mode;
		if (MODE_TRAIN_WIFI_SCANNIG == mode) {
			schedulemWifiScanTimer(MODE_TRAIN_WIFI_SCANNIG_PERIOD);
			if (locationScanner != null)
                locationScanner.stop();
		} else if (MODE_TRAIN_WIFI_FOUND == mode) {
			schedulemWifiScanTimer(MODE_TRAIN_WIFI_FOUND_PERIOD);
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
				: MODE_TRAIN_WIFI_SCANNIG;
		if (mode != newMode) {
			setMode(newMode);
		}
	}

	public void setLocationScanner(LocationScanner locationScanner) {
		this.locationScanner=locationScanner;
	}
}
