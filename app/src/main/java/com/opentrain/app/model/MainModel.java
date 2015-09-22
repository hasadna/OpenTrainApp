package com.opentrain.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by noam on 06/07/15.
 */
public class MainModel {

    private static MainModel mInstance;

    public static MainModel getInstance() {
        if (mInstance == null) {
            mInstance = new MainModel();
        }
        return mInstance;
    }

    // Map of BSSIDs to station names (future: change to station_id):
    private Map<String, String> bssidMap;
    // List of all the scanned stations at this trip:
    private List<Station> scannedStationList;
    private boolean inStation;
    // Test trip
    private ArrayList<ArrayList<WifiScanResultItem>> mockResultsList;
    // List of all train stations:
    private ArrayList<String> mStationList;

    private MainModel() {
        scannedStationList = new ArrayList<>();
        bssidMap = new HashMap<>();
        mockResultsList = new ArrayList<>();
        mStationList = new ArrayList<>();
    }

    public Map<String, String> getBssidMap() {
        return bssidMap;
    }

    public void setBssidMap(Map<String, String> bssidMap) {
        this.bssidMap = bssidMap;
    }

    public void updateMap(Map<String, String> results) {
        for (Map.Entry<String, String> serverEntry : results.entrySet()) {
            bssidMap.put(serverEntry.getKey(), serverEntry.getValue());
        }
    }

    public boolean isInStation() {
        return inStation;
    }

    public void setInStation(boolean inStation) {
        if (inStation && scannedStationList.isEmpty()) {
            throw new IllegalStateException("Cannot set isStation=true with empty station list.");
        }
        this.inStation = inStation;
    }

    // Returns null if not in station.
    public String getCurrentStationId() {
        if (inStation) {
            return scannedStationList.get(scannedStationList.size() - 1).getName();
        } else {
            return null;
        }
    }

    public List<Station> getScannedStationList() {
        return scannedStationList;
    }

    public void addStationAndSetInStation(Station station) {
        scannedStationList.add(station);
        inStation = true;
    }

    public void setScannedStationList(List<Station> scannedStationList) {
        this.scannedStationList = scannedStationList;
    }

    // Returns the last station's last seen time. If there is no last station, returns null.
    public Long getStationLastSeenTimeUnixMs() {
        if (scannedStationList.isEmpty()) {
            return null;
        }
        return scannedStationList.get(scannedStationList.size() - 1).lastSeenUnixTimeMs;
    }

    // Set the last station exit time to the last seen time. If the station list is empty, do nothing.
    public void setLastStationExitTimeIfItExists() {
        if (!scannedStationList.isEmpty()) {
            Station lastStation = scannedStationList.get(scannedStationList.size() - 1);
            lastStation.exitUnixTimeMs = lastStation.lastSeenUnixTimeMs;
        }
    }

    public void clearScannedItems() {
        scannedStationList.clear();
    }

    public ArrayList<ArrayList<WifiScanResultItem>> getMockResultsList() {
        return mockResultsList;
    }

    public void setMockResultsList(ArrayList<ArrayList<WifiScanResultItem>> mockResultsList) {
        this.mockResultsList = mockResultsList;
    }

    public void setStationList(ArrayList<String> stationList) {
        if (stationList != null && stationList.size() > 0) {
            this.mStationList = stationList;
        }
    }

    public ArrayList<String> getStationList() {
        return mStationList;
    }

}
