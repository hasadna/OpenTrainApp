package il.org.hasadna.opentrain.monitoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;
import il.org.hasadna.opentrain.*;

public class JsonDumper {
    	private static final String LOGTAG = JsonDumper.class.getName();
    	private String LOGTAG_PER_DUMPER = JsonDumper.class.getName();
    	private String mFileName;

		Context mContext;
		
		Boolean  mIsInitialized=false;
		FileWriter mFileWriter=null;
				
		public JsonDumper(Context context, String creatorTag)
		{
			mContext=context;
			LOGTAG_PER_DUMPER=LOGTAG+creatorTag;	

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			String currentDateandTime = sdf.format(new Date());
			
			
			mFileName=	currentDateandTime+ "." + creatorTag + ".txt";
		}
		
		public void open()
		{
			Log.d(LOGTAG_PER_DUMPER,"open:");

			if(!mIsInitialized)
			{
				try{
					createExternalStorageFile(mFileName);									
				}catch(Exception e){
					Log.w(LOGTAG_PER_DUMPER,"open:",e);
				}
				
				if(mIsInitialized)
				{
					dump("fileopenmarker",new JSONArray());
				}				
			}		
		}
		
		public void close() 
		{
			Log.d(LOGTAG_PER_DUMPER,"close:");

			if(mIsInitialized){
				try {
					mFileWriter.flush();
					mFileWriter.close();
				} catch (IOException e) {
					Log.e(LOGTAG_PER_DUMPER, "close:", e);
					e.printStackTrace();
				}
				finally{
					mFileWriter=null;
					mIsInitialized=false;
				}
			}
		}
		
		public void flush() {
			if(mIsInitialized){
				try {
					mFileWriter.flush();
				} catch (IOException e) {
					Log.e(LOGTAG_PER_DUMPER, "flush:", e);
					e.printStackTrace();
				}		
			}			
		}
		
		
		public static File getLogsDir(Context context){
			if(isExternalStorageMounted()){
				File dir = context.getExternalFilesDir("logs");
				Log.d(LOGTAG,"createExternalStoragePrivateFile: dir="+dir.toString());
				return dir;	
			}
			else{
				Log.w(LOGTAG,"getLogsDir: external stoage not mounted");
				return null;
			}
		}


		public void dump( Collection<ScanResult> scanResults )
		{
			if(!mIsInitialized){
				Log.d(LOGTAG_PER_DUMPER,"log Scan Result: Not initialized. ");

				return;
			}
			else{
				Log.d(LOGTAG_PER_DUMPER,"log Scan Result:");
				
				JSONArray hotspots = new JSONArray();
				for (ScanResult scanResult : scanResults) {
					try {
						JSONObject obj = new JSONObject();
						obj.put("SSID", scanResult.SSID);
						obj.put("key", scanResult.BSSID);
						obj.put("frequency", scanResult.frequency);
						obj.put("signal", scanResult.level);
						hotspots.put(obj);
					} catch (JSONException jsonex) {
						Log.e(LOGTAG_PER_DUMPER, "", jsonex);
					}
				 }
				dump("hotspots",hotspots);				
			}		
		}
		

		public void dump(Location location) {
			Log.d(LOGTAG_PER_DUMPER,"log Location:");
			
			JSONObject locationJson= new JSONObject();
	        if (location != null) {
	            try {
					locationJson.put("time", location.getTime());
		            locationJson.put("long", location.getLongitude());
		            locationJson.put("lat", location.getLatitude());
		            locationJson.put("provider", location.getProvider());
		            locationJson.put("accuracy", location.hasAccuracy() ? location.getAccuracy() : null);
		            locationJson.put("altitude", location.hasAltitude() ? location.getAltitude() : null);
		            locationJson.put("bearing", location.hasBearing() ? location.getBearing() : null);
		            locationJson.put("speed", location.hasSpeed() ? location.getSpeed() : null);
	            } catch (JSONException e) {
	        		Log.d(LOGTAG_PER_DUMPER,"log Location:",e);
					e.printStackTrace();
				}
	        }
	        dump("location", locationJson);
		}
						
		public void dump(String rawDataType, JSONArray rawJson)
		{
			if(!mIsInitialized){
				Log.d(LOGTAG_PER_DUMPER,"log JSONArray: Not Initialized");
				return;
			}
			else{
				Log.d(LOGTAG_PER_DUMPER,"log JSONArray:");
				
				JSONObject timestampedJson = new JSONObject();
				try {
					long timestamp=System.currentTimeMillis();
					timestampedJson.put("timestamp",timestamp);
					timestampedJson.put("date",DateTimeUtils.formatTimeForLocale(timestamp));
					
					timestampedJson.put(rawDataType,rawJson);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try{
					Write(timestampedJson.toString(2));					
				} 
				catch (JSONException e) {
					Log.w(LOGTAG_PER_DUMPER,"log JSONArray:",e);
					e.printStackTrace();
				}
				
			}
		}
		
		public void dump(String rawDataType, JSONObject rawJson)
		{
			if(!mIsInitialized){
				Log.d(LOGTAG_PER_DUMPER,"log JSONObject: Not Initialized");
				return;
			}
			else{
				Log.d(LOGTAG_PER_DUMPER,"log JSONObject:");
				
				JSONObject timestampedJson = new JSONObject();
				try {
					long timestamp=System.currentTimeMillis();
					timestampedJson.put("timestamp",timestamp);
					timestampedJson.put("date",DateTimeUtils.formatTimeForLocale(timestamp));
					
					timestampedJson.put(rawDataType,rawJson);
				} catch (JSONException jsonException) {
					Log.d(LOGTAG_PER_DUMPER,"log JSONObject:",jsonException);
					jsonException.printStackTrace();
				}
				
				try {
					Write(timestampedJson.toString(2));
				} catch (JSONException e) {
					Log.w(LOGTAG_PER_DUMPER,"log JSONObject:",e);
					e.printStackTrace();
				}
			}
		}
		
		private void Write(String string){
			try {
				mFileWriter.write(string);
				mFileWriter.flush();
			} catch (IOException e) {
				Log.d(LOGTAG_PER_DUMPER,"log JSONArray:",e);
				e.printStackTrace();
			}
		}
			
		void createExternalStorageFile(String fileName) {
			Log.d(LOGTAG_PER_DUMPER,"createExternalStoragePrivateFile:");

			File path = getLogsDir(mContext);
			
			if(path!=null && path.mkdir() || path.isDirectory()){
				Log.d(LOGTAG_PER_DUMPER,"createExternalStoragePrivateFile: mkdir");

				File file = new File(path,fileName);
				Log.d(LOGTAG_PER_DUMPER,"createExternalStoragePrivateFile: file="+file.getAbsolutePath());

				if (file!=null){
					try {
						mFileWriter = new FileWriter(file);
						mIsInitialized=true;
					} catch (IOException e) {
						Log.e(LOGTAG_PER_DUMPER, "", e);
						e.printStackTrace();
					}
				}
			}
		}
		
		static boolean isExternalStorageMounted() {
			Log.d(LOGTAG,"isExternalStorageMounted:");
		    String state = Environment.getExternalStorageState();
		    
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		        return true;
		    }
		    
			Log.d(LOGTAG,"isExternalStorageMounted: false");
		    return false;
		}

	
}
