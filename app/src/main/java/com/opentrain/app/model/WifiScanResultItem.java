package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by noam on 29/05/15.
 */
public class WifiScanResultItem {

    public final String BSSID;
    public final String SSID;


    public WifiScanResultItem(String BSSID, String SSID) {
        this.BSSID = BSSID;
        this.SSID = SSID;
    }

    public String toString() {
        return "BSSID: " + BSSID + " ,SSID: " + SSID;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("bssid", BSSID);
            json.put("ssid", SSID);
        } catch (JSONException exception) {
            Logger.log("toJson failed for WifiScanResultItem");
        }
        return json;
    }

    public static WifiScanResultItem fromJson(JSONObject json) {
        try {
            return new WifiScanResultItem(json.getString("bssid"), json.getString("sssid"));
        } catch (JSONException exception) {
            Logger.log("fromJson failed for WifiScanResultItem");
            return null;
        }
    }
}
