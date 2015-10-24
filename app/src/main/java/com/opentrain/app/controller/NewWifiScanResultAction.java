package com.opentrain.app.controller;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.utils.Logger;

/**
 * Created by Elina_2 on 23 Oct 2015.
 */
public class NewWifiScanResultAction implements Action {
    WifiScanResult scanResult;

    public NewWifiScanResultAction(WifiScanResult scanResult) {
        this.scanResult = scanResult;
    }

    void execute() {
        if (scanResult.wifiScanResultItems.size() > 0) {
            for (WifiScanResultItem wifiScanResultItem : scanResult.wifiScanResultItems) {
                if (wifiScanResultItem.SSID.equals(Settings.stationSSID)) {
                    String mapping = "Unmapped";
                    if (MainModel.getBssidMapping().containsKey(wifiScanResultItem.BSSID)) {
                        mapping = MainModel.getBssidMapping().get(wifiScanResultItem.BSSID);
                    }
                    Logger.log("scan result: " + wifiScanResultItem.toString() + ", mapping: " + mapping);
                }
            }
        }
        ScanResultProcessor.process(MainModel.getInstance(), scanResult);
    }

}
