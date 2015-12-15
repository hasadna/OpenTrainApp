package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Elina on 9/20/2015.
 */
public class WifiScanResult {
    public final long unixTimeMs;
    public final List<WifiScanResultItem> wifiScanResultItems;

    public WifiScanResult(long unixTimeMs, List<WifiScanResultItem> wifiScanResultItems) {
        this.unixTimeMs = unixTimeMs;
        this.wifiScanResultItems = wifiScanResultItems;
    }

    public WifiScanResult(long unixTimeMs) {
        this.unixTimeMs = unixTimeMs;
        this.wifiScanResultItems = new ArrayList<>();
    }

    public WifiScanResult(long unixTimeMs, String... bssidsSsidsArray) {
        if (bssidsSsidsArray.length % 2 != 0) {
            throw new IllegalArgumentException("BSSID and SSID array should have even length.");
        }
        this.unixTimeMs = unixTimeMs;
        wifiScanResultItems = new ArrayList<>();
        for (int i = 0; i < bssidsSsidsArray.length; i+=2) {
            wifiScanResultItems.add(
                    new WifiScanResultItem(bssidsSsidsArray[i], bssidsSsidsArray[i+1]));
        }
    }

    public WifiScanResult buildWithItems(List<WifiScanResultItem> wifiScanResultItems) {
        return new WifiScanResult(unixTimeMs, wifiScanResultItems);
    }

    public Set<String> getBssids() {
        Set<String> result = new HashSet<>();
        for (WifiScanResultItem item : wifiScanResultItems) {
            result.add(item.BSSID);
        }
        return result;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("unixTimeMs", unixTimeMs);
            JSONArray wifiScanResultItemsJsonArray = new JSONArray();
            for (int i = 0; i < wifiScanResultItems.size(); i++) {
                wifiScanResultItemsJsonArray.put(wifiScanResultItems.get(i).toJson());
            }
            json.put("wifiScanResultItems", wifiScanResultItemsJsonArray);
        } catch (JSONException exception) {
            Logger.log("toJson failed for WifiScanResult");
        }
        return json;
    }

    public static WifiScanResult fromJson(JSONObject json) {
        try {
            JSONArray wifiScanResultItemsJsonArray = json.getJSONArray("wifiScanResultItems");
            List<WifiScanResultItem> items = new ArrayList<>();
            for (int i = 0; i < wifiScanResultItemsJsonArray.length(); i++) {
                items.add(WifiScanResultItem.fromJson(wifiScanResultItemsJsonArray.getJSONObject(i)));
            }
            return new WifiScanResult(json.getLong("unixTimeMs"), items);
        } catch (JSONException exception) {
            Logger.log("fromJson failed for WifiScanResult");
            return null;
        }
    }

}
