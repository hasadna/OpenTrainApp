package com.opentrain.app.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Elina on 9/20/2015.
 */
public class WifiScanResult {
    public final List<WifiScanResultItem> wifiScanResultItems;
    public final long unixTimeMs;

    public WifiScanResult(List<WifiScanResultItem> wifiScanResultItems, long unixTimeMs) {
        this.wifiScanResultItems = wifiScanResultItems;
        this.unixTimeMs = unixTimeMs;
    }

    // Returns false iff there are two bssids mapped to different stations.
    // TODO: Extract isConsistent and hasUnmappedBssid into new BssidUtils class under utils folder.
    public boolean isConsistent(Map<String, String> bssidMap) {
        String stationId = null;
        for (WifiScanResultItem scanItem : wifiScanResultItems) {
            if (bssidMap.containsKey(scanItem.BSSID)) {
                if (stationId == null) {
                    stationId = bssidMap.get(scanItem.BSSID);
                } else if (stationId != bssidMap.get(scanItem.BSSID)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasUnmappedBssid(Map<String, String> bssidMap) {
        for (WifiScanResultItem scanItem : wifiScanResultItems) {
            if (!bssidMap.containsKey(scanItem.BSSID)) {
                return true;
            }
        }
        return false;
    }

    public WifiScanResult buildWithItems(List<WifiScanResultItem> wifiScanResultItems) {
        return new WifiScanResult(wifiScanResultItems, unixTimeMs);
    }

    public Set<String> getBssids() {
        Set<String> result = new HashSet<>();
        for (WifiScanResultItem item : wifiScanResultItems) {
            result.add(item.BSSID);
        }
        return result;
    }

}
