package com.opentrain.app.controller;

import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.WifiScanResult;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Elina on 23 Oct 2015.
 */
public class UpdateBssidMapAction implements Action {
    private BssidMap bssidMap;

    public UpdateBssidMapAction(BssidMap bssidMap) {
        this.bssidMap = bssidMap;
    }

    public void execute() {
        MainModel.getInstance().setBssidMap(bssidMap);
    }

    public JSONObject toJson() {
        return bssidMap.toJson();
    }

    public static UpdateBssidMapAction fromJson(JSONObject json) {
        return new UpdateBssidMapAction(BssidMap.fromJson(json));
    }
}
