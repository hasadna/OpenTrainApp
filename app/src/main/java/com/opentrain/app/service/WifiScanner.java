package com.opentrain.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.model.ScanResultProcessor;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noam on 18/05/15.
 */
public class WifiScanner extends BroadcastReceiver {

    WifiManager mainWifi;
    boolean registered;
    boolean wasStation;
    private ScanningListener scanningListener;

    public WifiScanner(Context context) {
        mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
    }

    public void startScanning() {
        mainWifi.startScan();
    }

    protected void unRegister(Context context) {
        if (registered) {
            context.unregisterReceiver(this);
            registered = false;
        }
    }

    protected void register(Context context) {
        context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registered = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        List<ScanResult> results = mainWifi.getScanResults();
        ArrayList<WifiScanResultItem> wifiScanResultItems = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (ScanResult scanResult : results) {
                WifiScanResultItem wifiScanResultItem =
                        new WifiScanResultItem(scanResult.BSSID, scanResult.SSID);
                wifiScanResultItems.add(wifiScanResultItem);
            }
        }
        reportScanResult(wifiScanResultItems);
    }

    public void reportScanResult(ArrayList<WifiScanResultItem> results) {
        if (results != null && results.size() > 0) {
            for (WifiScanResultItem wifiScanResultItem : results) {
                // TODO: Remove from the log the wifiScanResultItem of routers that are not stations
                Logger.log("scan result: " + wifiScanResultItem.toString());
            }
        }
        WifiScanResult scanResult = new WifiScanResult(results, System.currentTimeMillis());
        ScanResultProcessor.process(MainModel.getInstance(), scanResult);
        if (scanningListener != null) {
            scanningListener.onScanResult();
        }
    }

//    public void setName(Station station) {
//
//        for (Map.Entry<String, String> entry : station.bssids.entrySet()) {
//            if (entry.getValue() != null) {
//                station.stationName = entry.getValue();
//                return;
//            }
//        }
//
//        station.stationName = "Not found for any BSSID";
//    }

    public void setScanningListener(ScanningListener scanningListener) {
        this.scanningListener = scanningListener;
    }

    public interface ScanningListener {
        void onScanResult();
    }
}
