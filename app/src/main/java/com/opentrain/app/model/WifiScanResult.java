package com.opentrain.app.model;

import java.util.ArrayList;
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

    public WifiScanResult(long unixTimeMs) {
        this.wifiScanResultItems = new ArrayList<>();
        this.unixTimeMs = unixTimeMs;
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
