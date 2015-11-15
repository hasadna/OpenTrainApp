package com.opentrain.app.model;

import com.opentrain.app.utils.TimeUtils;

import org.json.JSONObject;

/**
 * Created by noam on 25/10/15.
 */
public class Stop {

    public long arrival;
    public long departure;
    public int s;
    public int index;

    public void parse(JSONObject stopJson) throws Exception {
        arrival = TimeUtils.getFormattedTime(stopJson.getString("a"));
        departure = TimeUtils.getFormattedTime(stopJson.getString("d"));
        s = stopJson.getInt("s");
        index = stopJson.getInt("i");
    }
}
