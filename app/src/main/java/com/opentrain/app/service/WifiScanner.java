package com.opentrain.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.ScanResultItem;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.network.NetowrkManager;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by noam on 18/05/15.
 */
public class WifiScanner extends BroadcastReceiver {

    WifiManager mainWifi;
    boolean registered;
    boolean wasStation;
    protected HashMap<String, String> map = new HashMap<>();
    ArrayList<Station> stationsListItems = new ArrayList<>();
    private ScanningListener scanningListener;

    public WifiScanner(Context context) {
        mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }

        getMapFromServer();
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
        ArrayList<ScanResultItem> scanResultItems = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (ScanResult scanResult : results) {
                ScanResultItem scanResultItem = new ScanResultItem();
                scanResultItem.BSSID = scanResult.BSSID;
                scanResultItem.SSID = scanResult.SSID;
                scanResultItems.add(scanResultItem);
            }
        }
        reportScanResult(scanResultItems);
    }

    public void reportScanResult(ArrayList<ScanResultItem> results) {
        if (results != null && results.size() > 0) {
            for (ScanResultItem scanResultItem : results) {
                Logger.log("scan result: " + scanResultItem.toString());
            }
        }
        onSanningResults(results);
        if (scanningListener != null) {
            scanningListener.onSannResult();
        }
    }


    public void onSanningResults(List<ScanResultItem> scanResults) {
        if (scanResults != null && scanResults.size() > 0) {

            boolean isStation = isStation(scanResults);

            if (isStation) {

                Station station = getStation(scanResults);
                setName(station);

                if (!wasStation) {
                    station.setArrive(System.currentTimeMillis());
                    stationsListItems.add(station);
                    Logger.log("enter to station: " + station.stationName);
                } else {
                    Logger.log("remain in station: " + station.stationName);
                }
            } else {
                if (wasStation) {
                    if (stationsListItems.size() > 0) {
                        Station s = stationsListItems.get(stationsListItems.size() - 1);
                        s.setDeparture(System.currentTimeMillis());
                    }
                    Logger.log("exit from station!");
                } else {
                    Logger.log("not station!");
                }
            }

            wasStation = isStation;
        }
    }

    public boolean isStation(List<ScanResultItem> scanResults) {

        for (ScanResultItem scanResult : scanResults) {
            if (Settings.stationSSID.equals(scanResult.SSID)) {
                return true;
            }
        }

        return false;
    }

    public Station getStation(List<ScanResultItem> scanResults) {

        Station station = null;
        for (ScanResultItem scanResult : scanResults) {

            if (Settings.stationSSID.equals(scanResult.SSID)) {

                if (station == null) {
                    station = new Station();
                }

                station.bssids.put(scanResult.BSSID, map.get(scanResult.BSSID));
            }
        }

        return station;
    }

    public void setName(Station station) {

        for (Map.Entry<String, String> entry : station.bssids.entrySet()) {
            if (entry.getValue() != null) {
                station.stationName = entry.getValue();
                return;
            }
        }

        station.stationName = "Not found fo any BSSID";
    }

    private Station updateBssidMapping(Station station) {
        if (map != null) {
            for (Map.Entry<String, String> entry : station.bssids.entrySet()) {
                station.bssids.put(entry.getKey(), map.get(entry.getKey()));
            }
        }
        return station;
    }

    public void getMapFromServer() {

        NetowrkManager.getInstance().getMapFromServer(new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {
                map = MainModel.getInstance().getMap();
                for (Station station : stationsListItems) {
                    updateBssidMapping(station);
                    setName(station);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    public void setScanningListener(ScanningListener scanningListener) {
        this.scanningListener = scanningListener;
    }

    public interface ScanningListener {
        void onSannResult();
    }
}
