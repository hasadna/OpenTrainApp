package il.org.hasadna.opentrain.monitoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

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
		
		String LOGTAG;
		Context mContext;
		
		Boolean  mIsInitialized=false;
		FileWriter mFileWriter=null;
				
		public JsonDumper(Context context, String logtag)
		{
			mContext=context;
			LOGTAG=logtag+".LogJson";			
		}
		
		public void open()
		{
			Log.d(LOGTAG,"open:");

			if(!mIsInitialized)
			{
				try{
					if(isExternalStorageWritable()){
						String fileName=String.valueOf(System.currentTimeMillis()) + "." + LOGTAG + ".json.txt";
						createExternalStorageFile(fileName);	
					}					
				}catch(Exception e){
					Log.w(LOGTAG,"open:",e);
				}
				
				if(mIsInitialized)
				{
					dump("fileopenmarker",new JSONArray());
				}				
			}		
		}
		
		public void close() 
		{
			Log.d(LOGTAG,"close:");

			if(mIsInitialized){
				try {
					mFileWriter.flush();
					mFileWriter.close();
				} catch (IOException e) {
					Log.e(LOGTAG, "close:", e);
					e.printStackTrace();
				}
				finally{
					mFileWriter=null;
					mIsInitialized=false;
				}
			}
		}


		public void dump( Collection<ScanResult> scanResults )
		{
			if(!mIsInitialized){
				Log.d(LOGTAG,"log Scan Result: Not initialized. ");

				return;
			}
			else{
				Log.d(LOGTAG,"log Scan Result:");
				
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
						Log.e(LOGTAG, "", jsonex);
					}
				 }
				dump("hotspots",hotspots);				
			}		
		}
		

		public void dump(Location location) {
			Log.d(LOGTAG,"log Location:");
			
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
	        		Log.d(LOGTAG,"log Location:",e);
					e.printStackTrace();
				}
	        }
	        dump("location", locationJson);
		}
						
		public void dump(String rawDataType, JSONArray rawJson)
		{
			if(!mIsInitialized){
				Log.d(LOGTAG,"log JSONArray: Not Initialized");
				return;
			}
			else{
				Log.d(LOGTAG,"log JSONArray:");
				
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
					Log.w(LOGTAG,"log JSONArray:",e);
					e.printStackTrace();
				}
				
			}
		}
		
		public void dump(String rawDataType, JSONObject rawJson)
		{
			if(!mIsInitialized){
				Log.d(LOGTAG,"log JSONObject: Not Initialized");
				return;
			}
			else{
				Log.d(LOGTAG,"log JSONObject:");
				
				JSONObject timestampedJson = new JSONObject();
				try {
					long timestamp=System.currentTimeMillis();
					timestampedJson.put("timestamp",timestamp);
					timestampedJson.put("date",DateTimeUtils.formatTimeForLocale(timestamp));
					
					timestampedJson.put(rawDataType,rawJson);
				} catch (JSONException jsonException) {
					Log.d(LOGTAG,"log JSONObject:",jsonException);
					jsonException.printStackTrace();
				}
				
				try {
					Write(timestampedJson.toString(2));
				} catch (JSONException e) {
					Log.w(LOGTAG,"log JSONObject:",e);
					e.printStackTrace();
				}
			}
		}
		
		private void Write(String string){
			try {
				mFileWriter.write(string);
				mFileWriter.flush();
			} catch (IOException e) {
				Log.d(LOGTAG,"log JSONArray:",e);
				e.printStackTrace();
			}
		}
		
		
		boolean isExternalStorageWritable() {
			Log.d(LOGTAG,"isExternalStorageWritable:");
		    String state = Environment.getExternalStorageState();
		    
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
				Log.d(LOGTAG,"isExternalStorageWritable: true");
		        return true;
		    }
		    
			Log.d(LOGTAG,"isExternalStorageWritable: false");
		    return false;
		}
	
		void createExternalStorageFile(String fileName) {
			Log.d(LOGTAG,"createExternalStoragePrivateFile:");

			File path = mContext.getExternalFilesDir("logs");
			Log.d(LOGTAG,"createExternalStoragePrivateFile: dir="+path.toString());

			if(path.mkdir() || path.isDirectory()){
				Log.d(LOGTAG,"createExternalStoragePrivateFile: mkdir");

				File file = new File(path,fileName);
				Log.d(LOGTAG,"createExternalStoragePrivateFile: file="+file.getAbsolutePath());

				if (file!=null){
					try {
						mFileWriter = new FileWriter(file);
						mIsInitialized=true;
					} catch (IOException e) {
						Log.e(LOGTAG, "", e);
						e.printStackTrace();
					}
				}
			}
		}
}
