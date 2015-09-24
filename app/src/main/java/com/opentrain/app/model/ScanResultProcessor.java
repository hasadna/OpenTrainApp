package com.opentrain.app.model;

import java.util.ArrayList;
import java.util.List;

import com.opentrain.app.utils.BssidUtils;
import com.opentrain.app.utils.Logger;

/**
 * This class is never instantiated.
 * Created by Elina on 9/20/2015.
 */
public class ScanResultProcessor {
    public static Settings DEFAULT_SETTINGS = new Settings("S-ISRAEL-RAILWAYS", 60 * 1000);

    // The processor modifies @model according to @scanResult.
    public static void process(MainModel model, WifiScanResult scanResult, Settings settings) {
        scanResult = cleanScanResult(scanResult, settings.ISRAEL_RAILWAYS_STATION_SSID);
        if (scanResult.wifiScanResultItems.isEmpty()) {
            if (model.getScannedStationList().isEmpty()) {

                Logger.log("Station list is empty, not updating anything.");
            } else {
                Logger.log("Updating last station exit time to last seen time and setting inStation to false.");
                model.setLastStationExitTimeIfItExists();
                model.setInStation(false);
            }
        } else {
            boolean hasUnmappedBssid = BssidUtils.hasUnmappedBssid(model.getBssidMap(), scanResult.getBssids());
            boolean scanResultConsistent = BssidUtils.isConsistent(model.getBssidMap(), scanResult.getBssids());
            if (hasUnmappedBssid || !scanResultConsistent) {
                Station lastStation = (model.getScannedStationList().isEmpty()) ?
                        null : model.getScannedStationList().get(model.getScannedStationList().size() - 1);
                // if (last station does not have the same bssid set as the WifiScanResult)
                if ((lastStation == null) || (!lastStation.getBssidsSet().equals(scanResult.getBssids()))) {
                    // Create a new station with an unknown name.
                    Logger.log(String.format("Creating a new unknown station. hasUnmappedBssid=%b, scanResultConsistent=%b.", hasUnmappedBssid, scanResultConsistent));
                    model.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), /*Station.UNKNOWN_STOP_ID, */scanResult.unixTimeMs);
                    model.addStationAndSetInStation(newStation);
                } else {
                    Logger.log("Still in the same unknown station, updating last seen time and setting exit time to null.");
                    lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                    lastStation.exitUnixTimeMs = null;
                    model.setInStation(true);

                }
            } else if (model.getStationLastSeenTimeUnixMs() != null &&
                    scanResult.unixTimeMs - model.getStationLastSeenTimeUnixMs() > settings.STATION_KEEPALIVE_MS) {
                // Create a new station with an unknown name.
                Logger.log("Creating a new station because we are out of keepalive (too much time has passed since last seen a station).");
                model.setLastStationExitTimeIfItExists();
                String scanStationId = model.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                model.addStationAndSetInStation(newStation);
            } else {
                // At this point we know that bssids are all mapped and homogenous.
                String scanStationId = model.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                if (model.getScannedStationList().isEmpty()) {
                    // Create a new station.
                    Logger.log("Creating a new station because the station list is empty.");
                    model.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                    model.addStationAndSetInStation(newStation);
                } else {
                    Station lastStation = model.getScannedStationList().get(model.getScannedStationList().size() - 1);
                    if (scanStationId.equals(lastStation.getName())) {
                        // Extend current station.
                        Logger.log("Still in the same station, updating last seen time and setting exit time to null.");
                        lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                        lastStation.exitUnixTimeMs = null;
                        model.setInStation(true);
                    } else {  // We changed stations
                        // Create a new station.
                        Logger.log("We changed stations. Creating a new station.");
                        model.setLastStationExitTimeIfItExists();
                        Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                        model.addStationAndSetInStation(newStation);
                    }
                }
            }
        }
    }

    public static void process(MainModel state, WifiScanResult scanResult) {
        process(state, scanResult, DEFAULT_SETTINGS);
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

    // TODO: Move settings to Settings class.
    public static class Settings {
        public final String ISRAEL_RAILWAYS_STATION_SSID;
        public final int STATION_KEEPALIVE_MS;

        public Settings(String israelRailwaysStationBssid, int stationKeepaliveMs) {
            ISRAEL_RAILWAYS_STATION_SSID = israelRailwaysStationBssid;
            STATION_KEEPALIVE_MS = stationKeepaliveMs;
        }
    }
}
