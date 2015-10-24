package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Elina_2 on 24 Oct 2015.
 */
public class BssidMap extends TreeMap<String, String> {

    public BssidMap() {
    }

    public BssidMap(Map<String,String> map) {
        this.putAll(map);
    }

    public BssidMap(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray("networks");
            for (int i = 0, j = jsonArray.length(); i < j; i++) {
                try {
                    JSONObject station = jsonArray.getJSONObject(i);
                    this.put(station.getString("bssid"), station.getString("name"));
                } catch (Exception e) {
                    Logger.log(e.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(e.toString());
        }
    }

    // Returns false iff there are two bssids mapped to different stations.
    public boolean isConsistent(Set<String> scanResultBssids) {
        String stationId = null;
        for (String scanBssid : scanResultBssids) {
            if (containsKey(scanBssid)) {
                if (stationId == null) {
                    stationId = get(scanBssid);
                } else if (!stationId.equals(get(scanBssid))) {
                    Logger.log(String.format("BSSIDs are not consistent. stationId1=%s, stationId2=%s.", stationId, get(scanBssid)));
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
}
