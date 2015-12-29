package com.opentrain.app.model;

import com.opentrain.app.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by noam on 07/06/15.
 */
public class LogItem {

    public String msg;
    public long unixTimeMs;

    public LogItem(String str) {
        this.msg = str;
        this.unixTimeMs = System.currentTimeMillis();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("unixTimeMs", unixTimeMs);
            json.put("message", msg);
        } catch (JSONException exception) {
            Logger.log("toJson failed for LogItem");
        }
        return json;
    }

}
