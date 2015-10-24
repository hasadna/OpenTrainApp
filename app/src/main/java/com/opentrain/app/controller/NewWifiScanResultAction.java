package com.opentrain.app.controller;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Elina on 23 Oct 2015.
 */
public class NewWifiScanResultAction implements Action {
    WifiScanResult scanResult;

    public NewWifiScanResultAction(WifiScanResult scanResult) {
        this.scanResult = scanResult;
    }

    void execute() {
        // Log
        for (WifiScanResultItem wifiScanResultItem : scanResult.wifiScanResultItems) {
            if (wifiScanResultItem.SSID.equals(Settings.stationSSID)) {
                String mapping = "Unmapped";
                if (MainModel.getBssidMapping().containsKey(wifiScanResultItem.BSSID)) {
                    mapping = MainModel.getBssidMapping().get(wifiScanResultItem.BSSID);
                }
                Logger.log("scan result: " + wifiScanResultItem.toString() + ", mapping: " + mapping);
            }
        }
        // Execute
        ScanResultProcessor.process(MainModel.getInstance(), scanResult);
    }

    public JSONObject toJson() {
        return scanResult.toJson();
    }

    public static NewWifiScanResultAction fromJson(JSONObject json) {
        return new NewWifiScanResultAction(WifiScanResult.fromJson(json));
    }
}
