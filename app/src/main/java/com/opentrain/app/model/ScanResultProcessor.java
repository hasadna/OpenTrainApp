package com.opentrain.app.model;

import java.util.ArrayList;
import java.util.List;
import com.opentrain.app.utils.Logger;

/**
 * This class is never instantiated.
 * Created by Elina on 9/20/2015.
 */
public class ScanResultProcessor {
    public static Settings DEFAULT_SETTINGS = new Settings("S-ISRAEL-RAILWAYS", 60);

    // The processor modifies @state according to @scanResult.
    public static void process(MainModel state, WifiScanResult scanResult, Settings settings) {
        scanResult = cleanScanResult(scanResult, settings.ISRAEL_RAILWAYS_STATION_SSID);
        if (scanResult.wifiScanResultItems.isEmpty()) {
            if (state.getScannedStationList().isEmpty()) {

                Logger.log("Station list is empty, not updating anything.");
            } else {
                Logger.log("Updating last station exit time to last seen time and setting inStation to false.");
                Station lastStation = state.getScannedStationList().get(state.getScannedStationList().size() - 1);
                lastStation.exitUnixTimeMs = lastStation.lastSeenUnixTimeMs;
                state.setInStation(false);
            }
        } else {
            boolean hasUnmappedBssid = scanResult.hasUnmappedBssid(state.getBssidMap());
            boolean scanResultConsistent = scanResult.isConsistent(state.getBssidMap());
            if (hasUnmappedBssid || !scanResultConsistent) {
                // Create a new station with an unknown name.
                Logger.log(String.format("Creating a new unknown station. hasUnmappedBssid=%b, scanResultConsistent=%b.", hasUnmappedBssid, scanResultConsistent));
                state.setLastStationExitTimeIfItExists();
                Station newStation = new Station(scanResult.getBssids(), /*Station.UNKNOWN_STOP_ID, */scanResult.unixTimeMs);
                state.addStationAndSetInStation(newStation);
            } else if (state.getStationLastSeenTimeUnixMs() != null &&
                    scanResult.unixTimeMs - state.getStationLastSeenTimeUnixMs() > settings.STATION_KEEPALIVE_MS) {
                // Create a new station with an unknown name.
                Logger.log("Creating a new station because we are out of keepalive (too much time has passed since last seen a station).");
                state.setLastStationExitTimeIfItExists();
                String scanStationId = state.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                state.addStationAndSetInStation(newStation);
            } else {
                // At this point we know that bssids are all mapped and homogenous.
                String scanStationId = state.getBssidMap().get(scanResult.wifiScanResultItems.get(0).BSSID);
                if (state.getScannedStationList().isEmpty()) {
                    // Create a new station.
                    Logger.log("Creating a new station because the station list is empty.");
                    state.setLastStationExitTimeIfItExists();
                    Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                    state.addStationAndSetInStation(newStation);
                } else {
                    Station lastStation = state.getScannedStationList().get(state.getScannedStationList().size() - 1);
                    if (scanStationId.equals(lastStation.getName())) {
                        // Extend current station.
                        Logger.log("Still in the same station, updating last seen time and setting exit time to null.");
                        lastStation.lastSeenUnixTimeMs = scanResult.unixTimeMs;
                        lastStation.exitUnixTimeMs = null;
                        state.setInStation(true);
                    } else {  // We changed stations
                        // Create a new station.
                        Logger.log("We changed stations. Creating a new station.");
                        state.setLastStationExitTimeIfItExists();
                        Station newStation = new Station(scanResult.getBssids(), /*scanStationId, */scanResult.unixTimeMs);
                        state.addStationAndSetInStation(newStation);
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
        public final String UNKNOWN_STATION_NAME = "לא ידוע";

        public Settings(String israelRailwaysStationBssid, int stationKeepaliveMs) {
            ISRAEL_RAILWAYS_STATION_SSID = israelRailwaysStationBssid;
            STATION_KEEPALIVE_MS = stationKeepaliveMs;
        }
    }
}
