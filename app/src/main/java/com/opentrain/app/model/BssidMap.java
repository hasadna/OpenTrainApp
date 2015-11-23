package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Elina on 24 Oct 2015.
 */
public class BssidMap extends HashMap<String, String> {

    public BssidMap() {
        super();
    }

    public BssidMap(Map<String,String> map) {
        super(map);
    }

    // Returns false iff there are two bssids mapped to different stations.
    public boolean isConsistent(Set<String> scanResultBssids) {
        String stationId = null;
        for (String scanBssid : scanResultBssids) {
            if (containsKey(scanBssid)) {
                if (stationId == null) {
                    stationId = get(scanBssid);
                } else if (!stationId.equals(get(scanBssid))) {
                    Logger.log("BSSIDs are not consistent:");
                    Logger.logMap(this);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasUnmappedBssid(Set<String> scanResultBssids) {
        for (String scanBssid : scanResultBssids) {
            if (!containsKey(scanBssid)) {
                Logger.log(String.format("BSSID is not mapped: scanBssid=%s", scanBssid));
                return true;
            }
        }
        return false;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }

    public static BssidMap fromJson(JSONObject json) {
        BssidMap bssidMap = new BssidMap();
        Iterator<String> keys = json.keys();
        try {
            while(keys.hasNext()) {
                String key = (String)keys.next();
                bssidMap.put(key, json.getString(key));
            }
        } catch (JSONException exception) {
            Logger.log("fromJson failed for BssidMap");
            return null;
        }
        return bssidMap;
    }
}
