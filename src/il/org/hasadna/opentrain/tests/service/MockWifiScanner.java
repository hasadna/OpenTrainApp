package il.org.hasadna.opentrain.tests.service;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import il.org.hasadna.opentrain.service.WifiNameFilter;
import il.org.hasadna.opentrain.service.WifiScanner;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockWifiScanner extends WifiScanner {

    public static final String[] WIFIS = {
            "S-ISRAEL-RAILWAYS",
            "S-ISRAEL-RAILWAYS",
            "ISRAEL-RAILWAYS",
            "ISRAEL-RAILWAYS",
            "S-ISRAEL-RAILWAYS",
            "NO_TRAIN",
            "NO_TRAIN",
            "NO_TRAIN",
    };
    private int index = 0;

    public MockWifiScanner(Context context) {
        super(context);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        //override parent and mock scan result
        JSONArray wifiInfo = new JSONArray();
        boolean isTrainIndication = false;
        boolean isStationIndication = false;
        long timestamp = System.currentTimeMillis();
        try {
            String ssid = WIFIS[index];
            if (index < WIFIS.length - 1) {
                index++;
            }
            if (WifiNameFilter.trainIndicatorsContain(ssid)) {
                JSONObject obj = new JSONObject();
                obj.put("SSID", ssid);
                obj.put("key", "34:08:04:73:6c:56");
                obj.put("frequency", 2437);
                obj.put("signal", -93);
                obj.put("timestamp", timestamp);
                wifiInfo.put(obj);
                isTrainIndication = true;
                if ("S-ISRAEL-RAILWAYS".equals(ssid)) {
                    isStationIndication = true;
                }
            }
        } catch (Exception ex) {

        }
        reportWifi(wifiInfo, isTrainIndication, isStationIndication);
    }

}
