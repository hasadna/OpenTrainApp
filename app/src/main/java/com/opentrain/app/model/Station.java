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

    @Override
    public String toString() {
        return this.stationName;
    }
}
