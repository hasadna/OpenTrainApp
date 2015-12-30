package com.opentrain.app.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by noam on 18/05/15.
 */
public class Station {

    public static final String UNKNOWN_STOP_ID = "UnknownId";
    public static final String UNKNOWN_STATION_NAME = "לא ידוע";
    // The bssids are from the first ScanResult that created the station.
    public Set<String> bssids = new HashSet<>();
    // The stationId (name) shall be taken from the mapping in the MainModel
    public long enterUnixTimeMs;
    public long lastSeenUnixTimeMs;
    public Long exitUnixTimeMs;

    public Station(Set<String> bssids, long enterUnixTimeMs) {
        this.bssids = bssids;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.lastSeenUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = null;
    }

    public Station(Set<String> bssids, long enterUnixTimeMs, long exitUnixTimeMs) {
        this.bssids = bssids;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.lastSeenUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = exitUnixTimeMs;
    }

    public String getBSSIDs() {

        StringBuilder stringBuilderUnMapped = new StringBuilder();

        for (String entry : bssids) {
            stringBuilderUnMapped.append(entry);
            stringBuilderUnMapped.append("\n");
        }

        return stringBuilderUnMapped.toString();
    }

    public Set<String> getBssidsSet() {
        return bssids;
    }

    // This function should return UNKNOWN_STOP_ID if not isConsistent or hasUnmappedBssid.
    public String getId() {

        boolean hasUnmappedBssid = MainModel.getBssidMapping().hasUnmappedBssid(bssids);
        boolean scanResultConsistent = MainModel.getBssidMapping().isConsistent(bssids);
        if ((!scanResultConsistent) || (hasUnmappedBssid)) {
            return UNKNOWN_STOP_ID;
        } else {
            for (String scanBssid : bssids) {
                return MainModel.getBssidMapping().get(scanBssid);
            }
            return UNKNOWN_STOP_ID;
        }
    }

    // Station name is UNKNOWN_STATION_NAME if getId() is UNKNOWN_STOP_ID, else it is getId();
    public String getName() {
        String id = getId();
        if (id == UNKNOWN_STOP_ID)
            return UNKNOWN_STATION_NAME;
        else {
            return MainModel.getInstance().getStopIdToStopMap().get(id);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
