package com.opentrain.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.NewWifiScanResultAction;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noam on 18/05/15.
 */
public class WifiScanner extends BroadcastReceiver {

    WifiManager mainWifi;
    boolean registered;
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
                // Add wifiScanResults iff SSID is "S-ISRAEL-RAILWAYS":
                if (scanResult.SSID.equals(Settings.stationSSID))
                    wifiScanResultItems.add(wifiScanResultItem);
            }
        }
        reportScanResult(new WifiScanResult(System.currentTimeMillis(), wifiScanResultItems));
    }

    public void reportScanResult(WifiScanResult scanResult) {
        reportScanResult(new NewWifiScanResultAction(scanResult));
    }

    public void reportScanResult(NewWifiScanResultAction action) {
        MainController.execute(action);
        if (scanningListener != null) {
            scanningListener.onScanResult();
        }
    }

    public void setScanningListener(ScanningListener scanningListener) {
        this.scanningListener = scanningListener;
    }

    public interface ScanningListener {
        void onScanResult();
    }
}
