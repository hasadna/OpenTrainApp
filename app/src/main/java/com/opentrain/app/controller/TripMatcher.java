package com.opentrain.app.controller;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.Stop;
import com.opentrain.app.model.Trip;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by noam on 15/11/15.
 */
public class TripMatcher {

    // Define how better the best match grade should be compared to second best:
    private static final double FACTOR_BETWEEN_MATCHED_TIMES = 1.5;

    public static Trip matchTrip() {
        List<Trip> todayTrips = MainModel.getInstance().getTrips();
        List<Station> scannedStations = MainModel.getInstance().getScannedStationList();

        Map<String, Stop> commonMap = new HashMap<>();
        Map<String, Stop> tripMap = new HashMap<>();

        MatchedTripDetails bestMatch = new TripMatcher().new MatchedTripDetails();
        MatchedTripDetails secondBestMatch = new TripMatcher().new MatchedTripDetails();

        // Loop over all todayTrips to compare them with scanned:
        for (Trip trip : todayTrips) {

            if (trip.stopTimes != null) {
                // Check if scannedStations and Trip contain the same stations:
                commonMap.clear();
                tripMap.clear();
                for (Stop tripStops : trip.stopTimes) {
                    tripMap.put(Integer.toString(tripStops.s), tripStops);
                }
                for (Station scannedStation : scannedStations) {
                    boolean match = tripMap.containsKey(scannedStation.getId());
                    if (match) {
                        commonMap.put(scannedStation.getId(), tripMap.get(scannedStation.getId()));

                    }
                }
                // Work only with lists longer than 2:
                List<Stop> matchingTripList = new ArrayList<>();
                List<Station> matchingScanList = new ArrayList<>();
                boolean tripMatch = false;
                if (commonMap.size() >= 2) {
                    // Check if stations order is correct:
                    for (Stop tripStop : trip.stopTimes) {
                        if (commonMap.containsKey(Integer.toString(tripStop.s))) {
                            matchingTripList.add(tripStop);
                        }
                    }
                    for (Station scannedStation : scannedStations) {
                        if (commonMap.containsKey(scannedStation.getId())) {
                            matchingScanList.add(scannedStation);
                        }
                    }
                    if (matchingTripList.size() == matchingScanList.size()) {
                        int idx=0;
                        while (idx < matchingTripList.size()) {
                            if (!Integer.toString(matchingTripList.get(idx).s)
                                    .equals(matchingScanList.get(idx).getId())) {
                                break;
                            }
                            idx++;
                        }
                        // when stations order is also correct, calculate the grade for that trip:
                        if (idx==matchingTripList.size()) {
                            // Save current trip and his grade:
                            long grade = calculateGrade(matchingTripList, matchingScanList);
                            if (grade < bestMatch.grade) {
                                secondBestMatch.set(bestMatch.id, bestMatch.grade, bestMatch.tripIndex);
                                bestMatch.set(trip.id, grade, todayTrips.indexOf(trip));
                            } else if (grade < secondBestMatch.grade) {
                                secondBestMatch.set(trip.id, grade, todayTrips.indexOf(trip));
                            }
                        }
                    }

                }
            }

        }

        // Now we have our best match, and it should be significantly better than all the others:
        if (bestMatch.grade != Long.MAX_VALUE) {
            if (bestMatch.grade*FACTOR_BETWEEN_MATCHED_TIMES <= secondBestMatch.grade) {
                Logger.log("Found match, trip id: " + bestMatch.id + ", grade: " + bestMatch.grade);
                return todayTrips.get(bestMatch.tripIndex);
            } else {
                Logger.log("Found match, trip id: " + bestMatch.id + ", but grade: " + bestMatch.grade + " is too close to second best grade: " + secondBestMatch.grade);
                return null;
            }
        } else {
            Logger.log("no trip match found");
            return null;
        }
    }

    private static long calculateGrade(List<Stop> stops, List<Station> stations) {
        // Assume the lists are the same length
        if (stops.size() != stations.size())
            return -1;

        int idx=0;
        long msOffset = 0;
        while (idx < stops.size()) {
            if (idx == 0) {
                // For first scanned station compare only exit time, as enter time might be long before the train arrives:
                msOffset += Math.abs(stations.get(idx).exitUnixTimeMs - stops.get(idx).departure);
            } else {
                msOffset += Math.abs(stations.get(idx).enterUnixTimeMs - stops.get(idx).arrival);
            }
            idx++;
        }
        return msOffset/stations.size();
    }

    private class MatchedTripDetails {
        public String id;
        public long grade;
        int tripIndex;

        public MatchedTripDetails() {
            this.id = "Unknown";
            this.grade = Long.MAX_VALUE;
            this.tripIndex = 0;
        }

        public void set(String id, long grade, int tripIndex) {
            this.id = id;
            this.grade = grade;
            this.tripIndex = tripIndex;
        }
    }

}
