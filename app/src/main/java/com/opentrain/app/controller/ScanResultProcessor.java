package com.opentrain.app.controller;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is never instantiated.
 * Created by Elina on 9/20/2015.
 */
public class ScanResultProcessor {

    // The processor modifies @model according to @scanResult.
    public static void process(MainModel model, WifiScanResult scanResult) {
        scanResult = cleanScanResult(scanResult, Settings.stationSSID);
        if (scanResult.wifiScanResultItems.isEmpty()) {
            if (model.getScannedStationList().isEmpty()) {
                Logger.log("Scan is empty and station list is empty, not updating anything.");
            } else if (!model.isInStation() && (model.getLastStationExitTime() != null)) {
                Logger.log("Scan is empty. Not updating anything.");
            } else {
                Logger.log("Scan is empty. Updating last station exit time to last seen time and setting inStation to false.");
                model.setLastStationExitTimeIfItExists();
                model.setInStation(false);
            }
        } else {
            boolean hasUnmappedBssid = model.getBssidMap().hasUnmappedBssid(scanResult.getBssids());
            boolean scanResultConsistent = model.getBssidMap().isConsistent(scanResult.getBssids());
            if (hasUnmappedBssid || !scanResultConsistent) {
                Station lastStation = (model.getScannedStationList().isEmpty()) ?
                        null : model.getScannedStationList().get(model.getScannedStationList().size() - 1);
                // if (last station does not have the same bssid set as the WifiScanResult)
                if ((lastStation == null) || (!lastStation.getBssidsSet().equals(scanResult.getBssids()))) {
                    // Create a new station with an unknown name.
                    Logger.log(String.format("Creating a new unknown station. hasUnmappedBssid=%b, scanResultConsistent=%b.", hasUnmappedBssid, scanResultConsistent));
                    model.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), scanResult.unixTimeMs);
                    model.addStationAndSetInStation(newStation);
                } else {
                    Logger.log("Still in the same unknown station, updating last seen time and setting exit time to null.");
                    lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                    lastStation.exitUnixTimeMs = null;
                    model.setInStation(true);
                }
            } else if (model.getStationLastSeenTimeUnixMs() != null &&
                    scanResult.unixTimeMs - model.getStationLastSeenTimeUnixMs() > Settings.SCAN_KEEPALIVE) {
                Station lastStation = model.getScannedStationList().get(model.getScannedStationList().size() - 1);
                String scanStationId = model.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                if (scanStationId.equals(lastStation.getId()) &&
                    scanResult.unixTimeMs - model.getStationLastSeenTimeUnixMs() < Settings.SCAN_KEEPALIVE_BETWEEN_STATIONS) {
                    // Extend current station - stayed here for long time
                    Logger.log("Still in the same station, updating last seen time and setting exit time to null.");
                    lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                    lastStation.exitUnixTimeMs = null;
                    model.setInStation(true);
                } else {
                    // Create a new station with an unknown name.
                    Logger.log("Creating a new station because we are out of keepalive (too much time has passed since last seen a station).");
                    model.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), scanResult.unixTimeMs);
                    model.addStationAndSetInStation(newStation);
                }
            } else {
                // At this point we know that bssids are all mapped and homogenous.
                String scanStationId = model.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                if (model.getScannedStationList().isEmpty()) {
                    // Create a new station.
                    Logger.log("Creating a new station because the station list is empty.");
                    model.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), scanResult.unixTimeMs);
                    model.addStationAndSetInStation(newStation);
                } else {
                    Station lastStation = model.getScannedStationList().get(model.getScannedStationList().size() - 1);
                    if (scanStationId.equals(lastStation.getId())) {
                        // Extend current station.
                        Logger.log("Still in the same station, updating last seen time and setting exit time to null.");
                        lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                        lastStation.exitUnixTimeMs = null;
                        model.setInStation(true);
                    } else {  // We changed stations
                        // Create a new station.
                        Logger.log("We changed stations. Creating a new station.");
                        model.setLastStationExitTimeIfItExists();
                        Station newStation = new Station(scanResult.getBssids(), scanResult.unixTimeMs);
                        model.addStationAndSetInStation(newStation);
                    }
                }
            }
        }
    }

    public static void process(MainModel state, WifiScanResult scanResult, String israelRailwaysStationBssid, long stationKeepaliveMs) {
        Settings.setSettings(israelRailwaysStationBssid, stationKeepaliveMs);
        process(state, scanResult);
    }
    public static void processDefaultSettings(MainModel state, WifiScanResult scanResult) {
        Settings.setDefaultettings();
        process(state, scanResult);
    }

    private static WifiScanResult cleanScanResult(WifiScanResult scanResult, String ssid) {
        List<WifiScanResultItem> wifiScanResultItems = new ArrayList<>();
        for (WifiScanResultItem item : scanResult.wifiScanResultItems) {
            if (item.SSID.equals(ssid)) {
                wifiScanResultItems.add(item);
            }
        }
        return scanResult.buildWithItems(wifiScanResultItems);
    }

}
