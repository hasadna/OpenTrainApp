package il.org.hasadna.opentrain.tests.service;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.org.hasadna.opentrain.service.WifiScanner;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockWifiScanner extends WifiScanner {


    public MockWifiScanner(Context context) {
        super(context);
    }

    @Override
    public void onReceive(Context c, Intent intent) {

        //override parent and mock scan result
        JSONArray wifiInfo = new JSONArray();
        boolean isTrainIndication = false;
        long timestamp = System.currentTimeMillis();
        try {
            JSONObject obj = new JSONObject();
            obj.put("SSID", "ISRAEL-RAILWAYS");
            obj.put("key", "34:08:04:73:6c:56");
            obj.put("frequency", 2437);
            obj.put("signal", -93);
            obj.put("timestamp", timestamp);
            wifiInfo.put(obj);
            isTrainIndication = true;
        } catch (JSONException jsonex) {

        }
        reportWifi(wifiInfo, isTrainIndication);
    }

}
