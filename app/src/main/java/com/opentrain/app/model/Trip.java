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

    public Trip(String id, List<Stop> stops) {
        this.id = id;
        this.stopTimes.addAll(stops);
    }

    public Trip(JSONObject tripJson) throws Exception {
        parse(tripJson);
    }

    @Override
    public String toString() {
        return "Trip id: " + id + " ,stops:" + stopTimes.size();
    }

    public void parse(JSONObject tripJson) throws Exception {
        id = tripJson.getString("id");
        JSONArray stopTimesJson = tripJson.getJSONArray("stop_times");
        for (int j = 0; j < stopTimesJson.length(); j++) {
            JSONObject stopJson = stopTimesJson.getJSONObject(j);
            stopTimes.add(new Stop(stopJson));
        }
    }

}
