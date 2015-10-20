package com.opentrain.app.utils;

import com.opentrain.app.model.WifiScanResultItem;

import java.util.Map;
import java.util.Set;

/**
 * Created by Elina on 9/21/2015.
 */
public class BssidUtils {
    // Returns false iff there are two bssids mapped to different stations.
    public static boolean isConsistent(Map<String, String> bssidMap, Set<String> scanResultBssids) {
        String stationId = null;
        for (String scanBssid : scanResultBssids) {
            if (bssidMap.containsKey(scanBssid)) {
                if (stationId == null) {
                    stationId = bssidMap.get(scanBssid);
                } else if (!stationId.equals(bssidMap.get(scanBssid))) {
                    Logger.log(String.format("BSSIDs are not consistent. stationId1=%s, stationId2=%s.", stationId, bssidMap.get(scanBssid)));
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean hasUnmappedBssid(Map<String, String> bssidMap, Set<String> scanResultBssids) {
        for (String scanBssid : scanResultBssids) {
            if (!bssidMap.containsKey(scanBssid)) {
                Logger.log(String.format("BSSID is not mapped: scanBssid=%s", scanBssid));
                return true;
            }
        }
        return false;
    }

}
