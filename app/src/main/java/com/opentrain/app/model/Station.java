package com.opentrain.app.model;

import com.opentrain.app.utils.BssidUtils;
import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    //public String stationId;
    public long enterUnixTimeMs;
    public long lastSeenUnixTimeMs;
    public Long exitUnixTimeMs;

    public Station(Set<String> bssids, /*String stopId, */long enterUnixTimeMs) {
        this.bssids = bssids;
        //this.stationId = stopId;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.lastSeenUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = null;
    }

    public Station(Set<String> bssids,/*String stopId, */long enterUnixTimeMs, long exitUnixTimeMs) {
        this.bssids = bssids;
        //this.stationId = stopId;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.lastSeenUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = exitUnixTimeMs;
    }






//    public HashMap<String, String> bssids = new HashMap<>();

//    public String stationName;
//    public long arrive;
//    public long departure;
//    public String arriveStr;
//    public String departureStr;

//    public void setArrive(long arrive) {
//        this.arrive = arrive;
//        arriveStr = TimeUtils.getFormattedTime();
//    }
//
//    public void setDeparture(long departure) {
//        this.departure = departure;
//        departureStr = TimeUtils.getFormattedTime();
//    }

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

//    public void setUnMappedBSSIDs(String str) {
//
//        try {
//            String[] bssidsStrings = str.split("\n");
//            for (String bssid : bssidsStrings) {
//                bssids.add(bssid);
//            }
//        } catch (Exception e) {
//            Logger.log(e.toString());
//        }
//    }

    public JSONObject getPostParam(String stationId) {

        JSONArray routerArray = new JSONArray();
        for (String entry : bssids) {
            routerArray.put(entry);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", stationId);
            jsonObject.put("bssid", routerArray.get(0));
        } catch (JSONException e) {
            Logger.log(e.toString());
        }

        return jsonObject;
    }

    // This function should return UNKNOWN_STOP_ID if not isConsistent or hasUnmappedBssid.
    public String getId() {

        boolean hasUnmappedBssid = BssidUtils.hasUnmappedBssid(MainModel.getBssidMapping(), bssids);
        boolean scanResultConsistent = BssidUtils.isConsistent(MainModel.getBssidMapping(), bssids);
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
        else
            return id;
    }

    @Override
    public String toString() {
        return getName();
    }

}
