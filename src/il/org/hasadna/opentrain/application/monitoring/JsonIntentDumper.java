package il.org.hasadna.opentrain.application.monitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.Collection;

import il.org.hasadna.opentrain.service.GPSScanner;
import il.org.hasadna.opentrain.service.WifiScanner;

public class JsonIntentDumper extends BroadcastReceiver {

    private static final String LOGTAG = JsonIntentDumper.class.getName();

    private JsonDumper mJsonDumper = null;
    ;

    public JsonIntentDumper(Context context) {
        mJsonDumper = new JsonDumper(context, "inner");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "onReceive:");
        String action = intent.getAction();


        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String data = intent.getStringExtra("data");

        if (data != null) {
            Log.d(LOGTAG, "onReceive: subject=" + subject + ", data=" + data);
        }

        if (action.equals(WifiScanner.ACTION_WIFIS_SCANNED)) {
            Collection<ScanResult> railWifiScanResults = intent.getParcelableArrayListExtra(WifiScanner.WIFI_SCANNER_ARG_SCANRESULT);
            mJsonDumper.dump(railWifiScanResults);
            Log.d(LOGTAG, "onReceive: Reporter data: wifi.");
        } else if (action.equals(GPSScanner.ACTION_GPS_UPDATED)) {
            Location location = intent.getParcelableExtra(GPSScanner.GPS_SCANNER_ARG_LOCATION);
            Log.d(LOGTAG, "onReceive: Reporter data: Location=" + location);
            mJsonDumper.dump(location);
        } else {
            Log.d(LOGTAG, "onReceive: Intent subject no recognized. Intent ignored. Subject=" + subject);
            return; // Intent not aimed at the Reporter (it is possibly for UI instead)
        }
    }

    public void open() {
        mJsonDumper.open();
    }

    public void close() {
        mJsonDumper.close();
    }

}
