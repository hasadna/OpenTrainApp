package il.org.hasadna.opentrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.org.hasadna.opentrain.preferences.Prefs;

public class Reporter extends BroadcastReceiver {
	private static final String LOGTAG = Reporter.class.getName();

	private final Context mContext;
	private final Prefs mPrefs;

	private long mLastTrainIndicationTime;

	private Location mLocation = null;

	private ReportUploader mReporterThread;


	public Reporter(Context context) {

		mContext = context;
		mPrefs = Prefs.getInstance(context);
		mLastTrainIndicationTime = 0;

		try {
			mReporterThread = new ReportUploader(mContext, mPrefs);
			mContext.registerReceiver(this, new IntentFilter(ScannerService.MESSAGE_TOPIC));
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(LOGTAG,"ctor: Failed on creating ReporterThread. Registration skipped. No reports would be generated.");
		}
	}

	public void shutdown() {
		Log("shutdown");
		mReporterThread.shutdown();
		mContext.unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log("onReceive:");
		String action = intent.getAction();

		//Verify intent action is relevant
		if (!action.equals(ScannerService.MESSAGE_TOPIC)) {
			Log.e(LOGTAG, "onReceive: Received an unknown intent. action="+action);
			return;
		}
		// We should only consider reporting if we are in a train context
		long time = intent.getLongExtra("time", 0);
		boolean isTrainIndication = intent.getBooleanExtra("TrainIndication", false);
		if (isTrainIndication) {
			mLastTrainIndicationTime = time;
			broadcastTrainIndicationStats();
		}

		if (System.currentTimeMillis() - mLastTrainIndicationTime > mPrefs.TRAIN_INDICATION_TTL) {
			// We are not in train context. Don't report.
			// TODO: turn off location API to save battery, add it back on when we're in a train context
			return;
		}

		String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		String data = intent.getStringExtra("data");

		if (data != null){
			Log("onReceive: subject=" + subject + ", data=" + data);
		}

		String wifiData = "";
		//Collection<ScanResult> wifiScanResults=null;////EyalLiebermann TODO: Remove usage of string and rely on Parcelable ArrayList after dumps prove its reliable 

		if (subject.equals(WifiScanner.WIFI_SCANNER_EXTRA_SUBJECT)) {
			wifiData = data;//EyalLiebermann TODO: Remove usage of string and rely on Parcelable ArrayList after dumps prove its reliable 
			// wifiScanResults=intent.getParcelableArrayListExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT);            
			Log("onReceive: Reporter data: WiFi.length=" + wifiData.length());
		} else if (subject.equals(LocationScanner.LOCATION_SCANNER_EXTRA_SUBJECT)) {
			mLocation = intent.getParcelableExtra(LocationScanner.LOCATION_SCANNER_ARG_LOCATION);
			Log("onReceive: Reporter data: Location=" + mLocation);
		} else {
			Log("Intent ignored. Subject=" + subject);
			return; // Intent not aimed at the Reporter (it is possibly for UI instead)
		}


		JSONObject report = buildReport(time, wifiData,mLocation);
		if(report!=null)
		{
			mReporterThread.report(report);
		}
	}

	private JSONObject buildReport(long time, String wifiInfo,  Location location) {
		// HASADNA: added this to enable debugging:
		//android.os.Debug.waitForDebugger();
		Log("buildReport:");
		JSONObject reportBuilder = null;
		//JSONArray cellJSON = null; 

		// Prepare the device id to be sent along with the report

		try {
			reportBuilder = new JSONObject();

			reportBuilder.put("time", time);
			if (location != null) {
				JSONObject locAPIInfo = new JSONObject();
				locAPIInfo.put("time", location.getTime());
				locAPIInfo.put("long", location.getLongitude());
				locAPIInfo.put("lat", location.getLatitude());
				locAPIInfo.put("provider", location.getProvider());
				locAPIInfo.put("accuracy", location.hasAccuracy() ? location.getAccuracy() : null);
				locAPIInfo.put("altitude", location.hasAltitude() ? location.getAltitude() : null);
				locAPIInfo.put("bearing", location.hasBearing() ? location.getBearing() : null);
				locAPIInfo.put("speed", location.hasSpeed() ? location.getSpeed() : null);
				reportBuilder.put("location_api", locAPIInfo);
			} else {
				reportBuilder.put("location_api", null);
			}

			String hashed_device_id = mPrefs.getDailyID();
			reportBuilder.put("device_id", hashed_device_id);

			reportBuilder.put("app_version_code", mPrefs.VERSION_CODE);
			reportBuilder.put("app_version_name", mPrefs.VERSION_NAME);
			reportBuilder.put("config_version", mPrefs.CONFIG_VERSION);

			JSONArray wifiJSON = null;
			if (wifiInfo.length() > 0) {
				wifiJSON = new JSONArray(wifiInfo);
				reportBuilder.put("wifi", wifiJSON);
			}
			if (wifiJSON == null ) {
				Log.w(LOGTAG, "buildReport: Invalid report: wifi or GPS entry is required. Report not built");
				return null;
			}

		} catch (JSONException jsonex) {
			Log.w(LOGTAG, "buildReport: JSONException caught. Report not built", jsonex);
			return null;
		}

		Log("buildReport: Report built successfully. reportBuilder.length="+reportBuilder.length());
		return reportBuilder;
	}


	//TODO: remove this interface and encapsulate logic in mReportThread class
	public void triggerUpload(){
		Log("triggerUpload:");
		mReporterThread.triggerUpload();
		broadcastTrainIndicationStats();
	}

	private void broadcastTrainIndicationStats() {
		Log("broadcastTrainIndicationStats:");
		Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
		i.putExtra(Intent.EXTRA_SUBJECT, Reporter.class.getName()+".trainIndication");
		i.putExtra(Reporter.class.getName()+".lastTrainIndicationTime", mLastTrainIndicationTime);
		//TODO: use extra data and remove the getters for lastUploadTime and reportsSent
		mContext.sendBroadcast(i);
	}

	private void Log(String msg)
	{
		Log.d(LOGTAG,msg);
	}

}
