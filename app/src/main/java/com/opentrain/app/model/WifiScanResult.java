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
