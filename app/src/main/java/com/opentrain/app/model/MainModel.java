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
            reset();
        }
        return mInstance;
    }

    public static void reset() {
        mInstance = new MainModel();
    }

    public static Map<String, String> getBssidMapping() {
        return getInstance().getBssidMap();
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
    //map bssid to stop id
    private HashMap<String, String> bssidToStopMap;
    // List of all today trips:
    private ArrayList<Trip> mTrips;
    // the current trip which matched by scanning and server data
    private Trip matchedTrip;

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

    // Aligns @scannedStations to @gtfsStations for display purposes in the UI
    // This method assumes the order of @gtfsStations is the same as @scannedStations.
    public List<MatchedStation> alignScannedTripToGtfsTrip(List<Station> scannedStations, List<GtfsStation> gtfsStations) {
        List<MatchedStation> result = new ArrayList<MatchedStation>();
        int gtfsIndex = 0;
        int lastMatchedGtfsIndex = 0;
        for (Station scanned : scannedStations) {
            while ((gtfsIndex < gtfsStations.size()) && (!scanned.getId().equals(gtfsStations.get(gtfsIndex).id))) {
                gtfsIndex++;
            }
            if (gtfsIndex < gtfsStations.size()) {
                // Found a match
                lastMatchedGtfsIndex = gtfsIndex;
                result.add(new MatchedStation(scanned, gtfsStations.get(gtfsIndex)));
            } else {
                // No match - but still display the scanned station data
                result.add(new MatchedStation(scanned, null));
                gtfsIndex = lastMatchedGtfsIndex; // Start from the the last matched station.
            }
        }
        return result;
    }

    public void setBssidToStopMap(HashMap<String, String> bssidToStopMap) {
        this.bssidToStopMap = bssidToStopMap;
    }

    public Map<String, String> getBssidToStopMap() {
        return bssidToStopMap;
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.mTrips = trips;
    }

    public ArrayList<Trip> getTrips() {
        return mTrips;
    }

    public void setMatchedTrip(Trip matchedTrip) {
        this.matchedTrip = matchedTrip;
    }

    public Trip getMatchedTrip() {
        return matchedTrip;
    }
}
