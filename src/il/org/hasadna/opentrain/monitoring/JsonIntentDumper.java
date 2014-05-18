package il.org.hasadna.opentrain.monitoring;

import java.util.Collection;

import il.org.hasadna.opentrain.LocationScanner;
import il.org.hasadna.opentrain.Scanner;
import il.org.hasadna.opentrain.ScannerService;
import il.org.hasadna.opentrain.WifiScanner;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

public class JsonIntentDumper extends BroadcastReceiver{
	
    private static final String LOGTAG = JsonIntentDumper.class.getName();

	private JsonDumper mJsonDumper= null;;
	
	public JsonIntentDumper(Context context) {
		mJsonDumper= new JsonDumper(context, LOGTAG);
		context.registerReceiver(this,new IntentFilter(ScannerService.MESSAGE_TOPIC));

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.d(LOGTAG,"onReceive:");
		String action = intent.getAction();
		
		//Verify intent action is relevant
		if (!action.equals(ScannerService.MESSAGE_TOPIC)) {
		    Log.e(LOGTAG, "onReceive: Received an unknown intent. action="+action);
		    return;
		}
		String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		String data = intent.getStringExtra("data");
		
		if (data != null){
			Log.d(LOGTAG,"onReceive: subject=" + subject + ", data=" + data);
		}		
		
		if (subject.equals(WifiScanner.WIFI_SCANNER_EXTRA_SUBJECT)) {
			Collection<ScanResult> railWifiScanResults = intent.getParcelableArrayListExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT);
			mJsonDumper.dump(railWifiScanResults);
		    Log.d(LOGTAG,"onReceive: Reporter data: wifi.");
		}  else if (subject.equals(LocationScanner.LOCATION_SCANNER_EXTRA_SUBJECT)) {
			Location location = intent.getParcelableExtra(LocationScanner.LOCATION_SCANNER_ARG_LOCATION);
		    Log.d(LOGTAG,"onReceive: Reporter data: Location=" + location);
		    mJsonDumper.dump(location);
		} else {
		    Log.d(LOGTAG,"onReceive: Intent subject no recognized. Intent ignored. Subject=" + subject);
		    return; // Intent not aimed at the Reporter (it is possibly for UI instead)
		}		
	}
	
	public void open(){
		mJsonDumper.open();
	}
	
	public void close(){
		mJsonDumper.close();
	}

}
