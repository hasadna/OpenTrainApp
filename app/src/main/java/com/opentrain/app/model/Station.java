package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;
import com.opentrain.app.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noam on 18/05/15.
 */
public class Station {
    public HashMap<String, String> bssids = new HashMap<>();

    public String stationName;
    public long arrive;
    public long departure;
    public String arriveStr;
    public String departureStr;

    public void setArrive(long arrive) {
        this.arrive = arrive;
        arriveStr = TimeUtils.getFormattedTime();
    }

    public void setDeparture(long departure) {
        this.departure = departure;
        departureStr = TimeUtils.getFormattedTime();
    }

    public boolean isEqual(Station other) {

        if (other == null) {
            return false;
        }

        for (Map.Entry<String, String> entry : bssids.entrySet()) {
            for (Map.Entry<String, String> otherEntry : other.bssids.entrySet()) {
                if (entry.getKey().equals(otherEntry.getKey())) {
                    return true;
                }
            }
        }

        if (stationName != null) {
            if (stationName.equals(other.stationName)) {
                return true;
            }
        }

        return false;
    }

    public void updateBssids(Station other) {
        for (Map.Entry<String, String> bssidEntry : other.bssids.entrySet()) {
            if (!bssids.containsKey(bssidEntry.getKey())) {
                bssids.put(bssidEntry.getKey(), bssidEntry.getValue());
            }
        }
    }

    public void updateExitTime() {
        setDeparture(System.currentTimeMillis());
    }

    public String toDetailString() {

        StringBuilder stringBuilderMapped = new StringBuilder();

        StringBuilder stringBuilderUnMapped = new StringBuilder();

        int mapped = 0;
        int unmapped = 0;

        for (Map.Entry<String, String> entry : bssids.entrySet()) {
            if (entry.getValue() != null) {
                if (mapped == 0) {
                    stringBuilderMapped.append("Mapped BSSID's:");
                    stringBuilderMapped.append("\n");
                }
                mapped++;
                stringBuilderMapped.append(entry.getKey());
                stringBuilderMapped.append("\n");
                stringBuilderMapped.append("(");
                stringBuilderMapped.append(entry.getValue());
                stringBuilderMapped.append(")");
                stringBuilderMapped.append("\n");
                stringBuilderMapped.append("\n");
            } else {
                if (unmapped == 0) {
                    stringBuilderUnMapped.append("Un-Mapped BSSID's:");
                    stringBuilderUnMapped.append("\n");
                }
                unmapped++;
                stringBuilderUnMapped.append(entry.getKey());
                stringBuilderUnMapped.append("\n");
                stringBuilderUnMapped.append("(");
                stringBuilderUnMapped.append(entry.getValue());
                stringBuilderUnMapped.append(")");
                stringBuilderUnMapped.append("\n");
            }

        }

        return stringBuilderMapped.toString() + "\n" + stringBuilderUnMapped.toString() + "\n";
    }

    public String getUnMappedBSSIDs() {

        StringBuilder stringBuilderUnMapped = new StringBuilder();

        for (Map.Entry<String, String> entry : bssids.entrySet()) {
            if (entry.getValue() == null) {
                stringBuilderUnMapped.append(entry.getKey());
                stringBuilderUnMapped.append("\n");
            }
        }

        return stringBuilderUnMapped.toString();
    }

    public String getBSSIDs() {

        StringBuilder stringBuilderUnMapped = new StringBuilder();

        for (Map.Entry<String, String> entry : bssids.entrySet()) {
            stringBuilderUnMapped.append(entry.getKey());
            stringBuilderUnMapped.append("\n");
        }

        return stringBuilderUnMapped.toString();
    }

    public void setUnMappedBSSIDs(String str) {

        try {
            String[] bssidsStrings = str.split("\n");
            for (String bssid : bssidsStrings) {
                bssids.put(bssid, null);
            }
        } catch (Exception e) {
            Logger.log(e.toString());
        }
    }

    public JSONObject getPostParam() {

        JSONArray routerArray = new JSONArray();
        for (Map.Entry<String, String> entry : bssids.entrySet()) {
            routerArray.put(entry.getKey());
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", stationName);
            jsonObject.put("bssid", routerArray.get(0));
        } catch (JSONException e) {
            Logger.log(e.toString());
        }

        return jsonObject;
    }
}
