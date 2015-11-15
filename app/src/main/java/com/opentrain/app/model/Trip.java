package com.opentrain.app.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by noam on 17/10/15.
 */
public class Trip {

    public String id;
    public List<Stop> stopTimes = new ArrayList<>();

    public void parse(JSONObject tripJson) throws Exception {
        id = tripJson.getString("id");
        JSONArray stopTimesJson = tripJson.getJSONArray("stop_times");
        for (int j = 0; j < stopTimesJson.length(); j++) {
            JSONObject stopJson = stopTimesJson.getJSONObject(j);
            Stop stop = new Stop();
            stop.parse(stopJson);
            stopTimes.add(stop);
        }
    }

    @Override
    public String toString() {
        return "Trip id: " + id + " ,stops:" + stopTimes.size();
    }
}
