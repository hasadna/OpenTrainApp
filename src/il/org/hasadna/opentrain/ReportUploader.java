package il.org.hasadna.opentrain;

import il.org.hasadna.opentrain.application.MainApplication;
import il.org.hasadna.opentrain.monitoring.JsonDumper;
import il.org.hasadna.opentrain.preferences.Prefs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

public class ReportUploader extends HandlerThread {
	private static final String LOGTAG = ReportUploader.class.getName();

	Handler mHandler;

	private JSONArray mReportArray;

	private URL mURL;
	// TODO make static once class is not more an innner class;
	private final String LOCATION_URL = "http://192.241.154.128/reports/add/";// "http://54.221.246.54/reports/add/";//"https://location.services.mozilla.com/v1/submit";
																				// //TODO:
																				// hasadna
																				// this
																				// should
																				// contain
																				// our
																				// own
																				// url
	private final String USER_AGENT_HEADER = "User-Agent";
	private final String DEVICE_ID_HEADER = "device_id";
	private final String TIMESTAMP_HEADER = "timestamp";
	private String USER_AGENT_STRING;

	private long mReportsSent = 0;
	public long mLastUploadTime = 0;

	private final long DELAY_RETRY_UPLOAD_ON_FAILURE = 30 * 1000;// 30 seconds
	private final long DELAY_PERIODIC_UPLOAD = 5 * 60 * 1000;// 30 seconds

	private JsonDumper mJsonDumper;

	private final Context mContext;
	private final Prefs mPrefs;

	class RunnableRetyUpload implements Runnable {
		@Override
		public void run() {
			upload();
		}
	}

	RunnableRetyUpload mRunnableRetryUpload = new RunnableRetyUpload();

	public ReportUploader(Context context, Prefs prefs) throws Exception {

		super(ReportUploader.class.getName());
		Log("ReporterThread: ctor");

		if (context == null || prefs == null) {
			throw new Exception(
					"Reporter context must be initialized before ReporterQueue creation");
		}
		mContext = context;
		mPrefs = prefs;

		String storedReports = mPrefs.getReports();
		mReportArray = null;
		try {
			mReportArray = new JSONArray(storedReports);
		} catch (Exception e) {
			mReportArray = new JSONArray();
		}

		String apiKey = PackageUtils.getMetaDataString(mContext,
				"il.org.hasadna.opentrain.API_KEY");
		try {
			mURL = new URL(LOCATION_URL + "?key=" + apiKey);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		USER_AGENT_STRING = NetworkUtils.getUserAgentString(mContext);

		mJsonDumper = new JsonDumper(mContext, "out.reports");
		mJsonDumper.open();

		start();
		mHandler = new Handler(getLooper());
		triggerUpload();// upload on looper thread
		schedulePeriodicUpload();
	}

	public void report(final JSONObject report) {
		Log("report:");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				enqueueReport(report);
				broadcastStats();
			}
		});
	}

	public void triggerUpload() {
		Log("triggerUpload:");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				upload();
				broadcastStats();
			}
		});
	}

	public void shutdown() {
		Log("shutDown:");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				broadcastStats();
				mPrefs.setReports(mReportArray.toString());
				mJsonDumper.close();
				quit();// Messages pending in looper queue dropped. Consider ROI
						// for fixing this.
			}
		});
	}

	// TODO revisit threading model. consider enqueue and check on main thread.
	// currently all actions except for getters are done on looper-handler
	// thread
	private void enqueueReport(JSONObject report) {
		Log("enqueueReport:");
		mReportArray.put(report);
		if (mReportArray.length() >= mPrefs.RECORD_BATCH_SIZE) {
			upload();
		}
	}

	class TestException extends Exception {
		public TestException() {
			super();
		}
	};

	private void upload() {
		Log("Upload:");
		if (0 == mReportArray.length()) {
			Log("Upload: No reports. Upload aborted.");
			return;
		}
		if (!NetworkUtils.isNetworkAvailable(mContext)) {
			Log.w(LOGTAG,
					"upload: Network is not available. Upload aborted. Retry scheduled.");
			scheduleRetryUpload();
			return;
		}

		String deviceId = mPrefs.getDailyID();
		String timestamp = Long.toString(System.currentTimeMillis());

		try {
			Log("Upload: reports.length=" + mReportArray.length());

			HttpURLConnection urlConnection = (HttpURLConnection) mURL
					.openConnection();

			try {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty(USER_AGENT_HEADER,
						USER_AGENT_STRING);

				urlConnection.setRequestProperty(DEVICE_ID_HEADER, deviceId);

				urlConnection.setRequestProperty(TIMESTAMP_HEADER, timestamp);

				String analyticsLogString = "timestamp=" + timestamp
						+ ", device_id= " + deviceId + ", totalReportsSent="
						+ mReportsSent + ", mReportArray.length="
						+ mReportArray.length();
				((MainApplication) mContext.getApplicationContext())
						.trackEvent("upload", analyticsLogString);

				JSONObject wrapper = new JSONObject();
				wrapper.put("items", mReportArray);
				String wrapperData = wrapper.toString();
				byte[] bytes = wrapperData.getBytes();
				urlConnection.setFixedLengthStreamingMode(bytes.length);
				OutputStream out = new BufferedOutputStream(
						urlConnection.getOutputStream());
				out.write(bytes);
				out.flush();

				mJsonDumper.dump("sentReports", mReportArray);

				Log("Upload: uploaded wrapperData: " + wrapperData + " to "
						+ mURL.toString());

				int responseCode = urlConnection.getResponseCode();
				if (responseCode >= 200 && responseCode <= 299) {
					mReportsSent = mReportsSent + mReportArray.length();
				}
				Log.i(LOGTAG, "Upload: Upload done. mTotalReportsSent="
						+ mReportsSent + ", mReportArray.length="
						+ mReportArray.length() + ", responseCode="
						+ responseCode);
				// }
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuilder total = new StringBuilder(in.available());
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}
				Log("Upload: response=" + total + "\n");

				r.close();

				mReportArray = new JSONArray();

				mLastUploadTime = System.currentTimeMillis();
				broadcastStats();
			} catch (JSONException jsonex) {
				Log.e(LOGTAG,
						"Upload: JSONException caught. Error wrapping data as a batch. Reports lost.",
						jsonex);
				mReportArray = new JSONArray();
			} catch (Exception ex) {
				Log.e(LOGTAG,
						"Upload: Exception caught. Error submitting data. Reschedule reports upload",
						ex);

				Toast.makeText(
						mContext,
						"Upload Error! Inner try block. Exception type="
								+ ex.getClass().getName(), Toast.LENGTH_LONG)
						.show();

				((MainApplication) mContext.getApplicationContext())
						.trackEvent("scheduleRetryUpload",
								"try-inner:" + ex.toString());

				scheduleRetryUpload();
			} finally {
				urlConnection.disconnect();
			}
		} catch (Exception ex) {
			Log.e(LOGTAG, "Upload: Error submitting data. Reschedule upload",
					ex);
			((MainApplication) mContext.getApplicationContext()).trackEvent(
					"scheduleRetryUpload", "try-outer:" + ex.toString());
			Toast.makeText(
					mContext,
					"Upload Error! Outer try block. Exception type="
							+ ex.getClass().getName(), Toast.LENGTH_LONG)
					.show();
			scheduleRetryUpload();
		}
	}

	private void schedulePeriodicUpload() {
		Log("schedulePeriodicUpload:");
		mHandler.postDelayed(new Runnable() {// Invariant: Always a single
												// periodic runnable in handler
												// queue

					@Override
					public void run() {
						upload();
						schedulePeriodicUpload();
					}
				}, DELAY_PERIODIC_UPLOAD);
	}

	private void scheduleRetryUpload() {
		Log("scheduleRetryUpload:");
		mHandler.removeCallbacks(mRunnableRetryUpload);// Avoid increasing
														// number of retry
														// runnable in handler
														// queue;
		mHandler.postDelayed(mRunnableRetryUpload,
				DELAY_RETRY_UPLOAD_ON_FAILURE);
	}

	private void broadcastStats() {
		Log("broadcastStats:");
		Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
		i.putExtra(Intent.EXTRA_SUBJECT, Reporter.class.getName() + ".upload");
		i.putExtra(Reporter.class.getName() + ".lastUploadTime",
				mLastUploadTime);
		i.putExtra(Reporter.class.getName() + ".reportsSent", mReportsSent);
		i.putExtra(Reporter.class.getName() + ".reportsPending",
				(long) mReportArray.length());

		Log("broadcastStats: MESSAGE_TOPIC=" + ScannerService.MESSAGE_TOPIC
				+ ", EXTRA_SUBJECT=.upload, .lastUploadTime=" + mLastUploadTime
				+ ", reportsSent=" + mReportsSent + ",reportsPending="
				+ mReportArray.length());

		mContext.sendBroadcast(i);
	}

	void Log(String msg) {
		Log.d(LOGTAG, msg);
	}

}
