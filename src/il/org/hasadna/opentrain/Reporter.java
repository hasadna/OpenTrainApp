package il.org.hasadna.opentrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

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

import il.org.hasadna.opentrain.preferences.Prefs;

class Reporter extends BroadcastReceiver {
    private static final String LOGTAG = Reporter.class.getName();

    private final Context mContext;
    private final Prefs mPrefs;

    private long mLastTrainIndicationTime;

    private Location mLocation = null;
    
    ReporterThread mReporterThread;

    Reporter(Context context) {

        mContext = context;
        mPrefs = Prefs.getInstance(context);
        mLastTrainIndicationTime = 0;
        
        try {
			mReporterThread = new ReporterThread();
	        mContext.registerReceiver(this, new IntentFilter(ScannerService.MESSAGE_TOPIC));
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(LOGTAG,"ctor: Failed on creating ReporterThread. Registration skipped. No reports would be generated.");
		}
    }

    void shutdown() {
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
            sendTrainIndicationIntent();
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
        String cellData = "";
        String radioType = "";
        String GPSData = "";
        if (subject.equals("WifiScanner")) {
            wifiData = data;
            Log("onReceive: Reporter data: WiFi.length=" + wifiData.length());
        } else if (subject.equals("CellScanner")) {
            cellData = data;
            radioType = intent.getStringExtra("radioType");
            Log("onReceive: Reporter data: Cell: " + cellData.length() + " (" + radioType + ")");
        } else if (subject.equals("GPSScanner")) {
            GPSData = data;
            Log("onReceive: Reporter data: GPS.length=" + GPSData.length());
        } else if (subject.equals(LocationScanner.LOCATION_SCANNER_EXTRA_SUBJECT)) {
            mLocation = intent.getParcelableExtra(LocationScanner.LOCATION_SCANNER_ARG_LOCATION);
            Log("onReceive: Reporter data: Location=" + mLocation);
        } else {
            Log("Intent ignored. Subject=" + subject);
            return; // Intent not aimed at the Reporter (it is possibly for UI instead)
        }

        // HASADNA: removed the following condition because we want to send data even when no gps is available.
        //if (mGPSData.length() > 0 && (mWifiData.length() > 0 || mCellData.length() > 0)) ...
        //TODO Verify location logic. Note it is a member variable and thus is included also on following reports
        JSONObject report = buildReport(time, GPSData, wifiData, radioType, cellData, mLocation);
        if(report!=null)
        {
        	mReporterThread.report(report);
        }
    }
    
	private JSONObject buildReport(long time, String gpsLocation, String wifiInfo, String radioType, String cellInfo, Location location) {
        // HASADNA: added this to enable debugging:
        //android.os.Debug.waitForDebugger();
        Log("buildReport:");
        JSONObject reportBuilder = null;
        //JSONArray cellJSON = null; 

        // Prepare the device id to be sent along with the report

        try {
            if (gpsLocation.length() > 0) {
                reportBuilder = new JSONObject(gpsLocation);
            } else {
                reportBuilder = new JSONObject();
            }

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

            reportBuilder.put("time", time);//TODO remove repetition
            
            String hashed_device_id = mPrefs.getDailyID();
            reportBuilder.put("device_id", hashed_device_id);

            reportBuilder.put("app_version_code", mPrefs.VERSION_CODE);
            reportBuilder.put("app_version_name", mPrefs.VERSION_NAME);
            reportBuilder.put("config_version", mPrefs.CONFIG_VERSION);

            // commenting out all CellScanner usage for now:            
//            if (cellInfo.length()>0) {
//                cellJSON=new JSONArray(cellInfo);
//                locInfo.put("cell", cellJSON);
//                locInfo.put("radio", radioType);
//            }
       		JSONArray wifiJSON = null;
            if (wifiInfo.length() > 0) {
                wifiJSON = new JSONArray(wifiInfo);
                reportBuilder.put("wifi", wifiJSON);
            }
            if (wifiJSON == null && gpsLocation.length() == 0) {
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
        Log("considerUpload:");
		mReporterThread.triggerUpload();
	}
	 
//	//TODO: remove this interface and send info as part of intent
//	public long getLastUploadTime() {
//        Log("getLastUploadTime:");
//        return mReporterThread.getLastUploadTime();
//    }
//	
//	//TODO: remove this interface and send info as part of intent
//    public long getReportsSent() {
//        Log("getReportsSent:");
//        return mReporterThread.getReportsSent();
//    }

//    public long getLastTrainIndicationTime() {
//        Log("getLastTrainIndicationTime:");
//        return mLastTrainIndicationTime;
//    }
    private void sendTrainIndicationIntent() {
		Log("sendTrainIndicationIntent:");
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
    
    
    class ReporterThread extends HandlerThread{
        private static final String LOGTAG = "il.org.opentrain.hasadna.ReporterThread";//Reporter.class.getName();

    	Handler mHandler;
		
	    private JSONArray mReportArray;
	    
	    private URL mURL;
	    //TODO make static once class is not more an innner class;
	    private  final String LOCATION_URL = "http://192.241.154.128/reports/add/";//"http://54.221.246.54/reports/add/";//"https://location.services.mozilla.com/v1/submit"; //TODO: hasadna this should contain our own url
	    private  final String USER_AGENT_HEADER = "User-Agent";
	    private String MOZSTUMBLER_USER_AGENT_STRING;
		
	//    private long mLastUploadTime;			
	    private long mReportsSent;
	    
	    private  final long DELAY_RETRY_UPLOAD_ON_FAILURE = 30*1000;//30 seconds
		private  final long DELAY_PERIODIC_UPLOAD = 5*60*1000;//30 seconds

	    class RunnableRetyUpload implements Runnable{
			@Override
			public void run() {
				upload();				
			}
		}
	    RunnableRetyUpload mRunnableRetryUpload= new  RunnableRetyUpload();
		
    	
		public ReporterThread() throws Exception {
			super(ReporterThread.class.getName());
			Log("ReporterThread: ctor");

			if(Reporter.this.mContext==null || Reporter.this.mPrefs==null){
				throw new Exception("Reporter context must be initialized before ReporterQueue creation");
			}
			
			String storedReports = mPrefs.getReports();	     
			mReportArray=null;
	        try {
	        	mReportArray = new JSONArray(storedReports);
	        } catch (Exception e) {
	        	mReportArray = new JSONArray();
	        }
	        
	        String apiKey = PackageUtils.getMetaDataString(mContext, "il.org.hasadna.opentrain.API_KEY");
	        try {
	            mURL = new URL(LOCATION_URL + "?key=" + apiKey);
	        } catch (MalformedURLException e) {
	            throw new IllegalArgumentException(e);
	        }
	        MOZSTUMBLER_USER_AGENT_STRING = NetworkUtils.getUserAgentString(mContext);
			
	        start();
			mHandler= new Handler(getLooper());	
			triggerUpload();//upload on looper thread
			schedulePeriodicUpload();
		}			
	
		public void report(final JSONObject report){
			Log("report:");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					enqueueReport(report);
				}
			});
		}
		public void triggerUpload(){
			Log("triggerUpload:");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					upload();					
				}
			});
		}
		
		public void shutdown()
		{
			Log("shutDown:");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
		    		Reporter.this.mPrefs.setReports(mReportArray.toString());
		    		quit();//Messages pending in looper queue dropped. Consider ROI for fixing this. 
				}
			});
		}
		
//		//TODO send as part of intent data
//	    synchronized public long getReportsSent() {
//			Log("getReportsSent:");
//			return mReportsSent;
//		}
//
//
//		//TODO send as part of intent data
//		synchronized public long getLastUploadTime() {
//			Log("getLastUploadTime:");
//			return mLastUploadTime;
//		}

	    //TODO revisit threading model. consider enqueue and check on main thread. currently all actions except for getters are done on looper-handler thread
		private void enqueueReport(JSONObject report){
			Log("enqueueReport:");
    		mReportArray.put(report);
    		if(	mReportArray.length() >= mPrefs.RECORD_BATCH_SIZE){
    			upload();
    		}
		}

		private void upload(){
			Log("Upload:");
			 if(0==mReportArray.length())
			 {
			    Log("Upload: No reports. Upload aborted.");
			    return;
			 }
			 if (!NetworkUtils.isNetworkAvailable(mContext)) {
			        Log.w(LOGTAG,"upload: Network is not available. Upload aborted. Retry scheduled.");
			        scheduleRetryUpload();
			        return;
			 }
			 try {
                   Log("Upload: reports.length="+mReportArray.length());
                    

                    HttpURLConnection urlConnection = (HttpURLConnection) mURL.openConnection();
                    try {
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty(USER_AGENT_HEADER, MOZSTUMBLER_USER_AGENT_STRING);

                        JSONObject wrapper = new JSONObject();
                        wrapper.put("items", mReportArray);
                        String wrapperData = wrapper.toString();
                        byte[] bytes = wrapperData.getBytes();
                        urlConnection.setFixedLengthStreamingMode(bytes.length);
                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                        out.write(bytes);
                        out.flush();

                        Log("Upload: uploaded wrapperData: " + wrapperData + " to " + mURL.toString());

                   // 	synchronized (this) {//TODO remove synchronization block when removing getters after putting data on intent
	                        int returnCode = urlConnection.getResponseCode();
	                        if (returnCode >= 200 && returnCode <= 299) {
		                            mReportsSent = mReportsSent + mReportArray.length();									
							}	                        
	                        Log.i(LOGTAG, "Upload: Upload done. mReportsSent="+mReportsSent+", mReportArray.length="+mReportArray.length()+", returnCode=" + returnCode);
                  //  	}
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder(in.available());
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line);
                        }
                        Log("Upload: response=" + total + "\n");
                      
                        r.close();

                        mReportArray =new JSONArray();

//                        synchronized(this){//TODO remove synchronization block when removing getters after putting data on intent
//                        	mLastUploadTime = System.currentTimeMillis();	
//                        }
               
                        sendUpdateIntent();
                    } catch (JSONException jsonex) {
                    	Log.e(LOGTAG, "Upload: JSONException caught. Error wrapping data as a batch. Reports lost.", jsonex);
                        mReportArray =new JSONArray();
                    } catch (Exception ex) {
                        Log.e(LOGTAG, "Upload: Exception caught. Error submitting data. Reschedule reports upload", ex);
                        scheduleRetryUpload();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception ex) {
                    Log.e(LOGTAG, "Upload: Error submitting data. Reschedule upload", ex);
                    scheduleRetryUpload();
                }
		}
		
		private void schedulePeriodicUpload(){
			Log("schedulePeriodicUpload:");
			mHandler.postDelayed(new Runnable() {//Invariant: Always a single periodic runnable in handler queue
				
				@Override
				public void run() {
					upload();
					schedulePeriodicUpload();
				}
			},DELAY_PERIODIC_UPLOAD );
		}
		private void scheduleRetryUpload() {
			Log("scheduleRetryUpload:");
			mHandler.removeCallbacks(mRunnableRetryUpload);//Avoid increasing number of retry runnable in handler queue;
			mHandler.postDelayed(mRunnableRetryUpload,DELAY_RETRY_UPLOAD_ON_FAILURE);
		}

	    private void sendUpdateIntent() {
			Log("sendUpdateIntent:");
	        Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
	        i.putExtra(Intent.EXTRA_SUBJECT, Reporter.class.getName()+".upload");
	        i.putExtra(Reporter.class.getName()+".lastUploadTime", System.currentTimeMillis());
	        i.putExtra(Reporter.class.getName()+".reportsSent", mReportsSent);
	        //TODO: use extra data and remove the getters for lastUploadTime and reportsSent
	        mContext.sendBroadcast(i);
	    }
		
		void Log(String msg){
			Log.d(LOGTAG, msg);
		}
		
    }
}
