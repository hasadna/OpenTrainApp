package com.opentrain.app.model;

import com.opentrain.app.controller.Action;
import com.opentrain.app.controller.NewWifiScanResultAction;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static BssidMap getBssidMapping() {
        return getInstance().getBssidMap();
    }

    // Map of BSSIDs to station names (future: change to station_id):
    private BssidMap bssidMap;
    // List of all the scanned stations at this trip:
    private List<Station> scannedStationList;
    private boolean inStation;
    // List of all train stations:
    private List<StationBasicInfo> mStationList;
    private List<Action> mHistory;
    // map StopId to StopName
    private HashMap<String, String> stopIdToStopMap;
    // List of all today trips:
    private List<Trip> mTrips;
    // the current trip which matched by scanning and server data
    private Trip matchedTrip;

    private MainModel() {
        scannedStationList = new ArrayList<>();
        bssidMap = new BssidMap();
        mStationList = new ArrayList<>();
        mHistory = new ArrayList<>();
        mTrips = new ArrayList<>();
        stopIdToStopMap = new HashMap<>();
    }

    public BssidMap getBssidMap() {
        return bssidMap;
    }

    public void setBssidMap(BssidMap bssidMap) {
        this.bssidMap = bssidMap;
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

    public Long getLastStationExitTime() {
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

    public void setStationList(List<StationBasicInfo> stationList) {
        if (stationList != null && stationList.size() > 0) {
            this.mStationList = stationList;
        }
    }

    public List<StationBasicInfo> getStationList() {
        return mStationList;
    }

    // Aligns @scannedStations to @gtfsStations for display purposes in the UI
    // This method assumes the order of @gtfsStations is the same as @scannedStations.
    public List<MatchedStation> alignScannedTripToGtfsTrip(List<Station> scannedStations, Trip trip) {
        List<MatchedStation> result = new ArrayList<MatchedStation>();
        // If no trip is matched:
        if (trip == null) {
            for (Station scanned : scannedStations) {
                result.add(new MatchedStation(scanned, null));
            }
            return result;
        }
        int gtfsIndex = 0;
        int lastMatchedGtfsIndex = 0;
        for (Station scanned : scannedStations) {
            while ((gtfsIndex < trip.stopTimes.size()) && (!scanned.getId().equals(Integer.toString(trip.stopTimes.get(gtfsIndex).s)))) { // was id -> index?
                gtfsIndex++;
            }
            if (gtfsIndex < trip.stopTimes.size()) {
                // Found a match
                lastMatchedGtfsIndex = gtfsIndex;
                result.add(new MatchedStation(scanned, trip.stopTimes.get(gtfsIndex)));
            } else {
                // No match - but still display the scanned station data
                result.add(new MatchedStation(scanned, null));
                gtfsIndex = lastMatchedGtfsIndex; // Start from the the last matched station.
            }
        }
        return result;
    }

    public void addToHistory(Action action) {
        mHistory.add(action);
    }

    public JSONObject historyToJson() {
        JSONObject json = new JSONObject();
        try {
            JSONArray actionsJson = new JSONArray();
            for (int i = 0; i < mHistory.size(); i++) {
                JSONObject singleActionJson = new JSONObject();
                singleActionJson.put(mHistory.get(i).getClass().getSimpleName(),
                        mHistory.get(i).toJson());
                actionsJson.put(singleActionJson);
            }
            json.put("actions", actionsJson);
        } catch (JSONException exception) {
            Logger.log("toJson failed for MainModel");
        }
        return json;
    }

    public static List<Action> historyFromJson(JSONObject json) {
        List<Action> history = new ArrayList<>();
        try {
            JSONArray actionsJson = json.getJSONArray("actions");
            for (int i = 0; i < actionsJson.length(); i++) {
                JSONObject singleActionJson = actionsJson.getJSONObject(i);
                if (singleActionJson.has("NewWifiScanResultAction")) {
                    history.add(NewWifiScanResultAction.fromJson(
                            (JSONObject)singleActionJson.get("NewWifiScanResultAction")));
                } else if (singleActionJson.has("UpdateBssidMapAction")) {
                    history.add(UpdateBssidMapAction.fromJson(
                            (JSONObject)singleActionJson.get("UpdateBssidMapAction")));
                } else {
                    throw new IllegalArgumentException("Unknown action class");
                }
            }
        } catch (JSONException exception) {
            Logger.log("toJson failed for MainModel");
        }
        return history;
    }

    public void setStopIdToStopMap(HashMap<String, String> stopIdToStopMap) {
        this.stopIdToStopMap = stopIdToStopMap;
    }

    public Map<String, String> getStopIdToStopMap() {
        return stopIdToStopMap;
    }

    public void setTrips(List<Trip> trips) {
        this.mTrips = trips;
    }

    public List<Trip> getTrips() {
        return mTrips;
    }

    public void setMatchedTrip(Trip matchedTrip) {
        this.matchedTrip = matchedTrip;
    }

    public Trip getMatchedTrip() {
        return matchedTrip;
    }

}
