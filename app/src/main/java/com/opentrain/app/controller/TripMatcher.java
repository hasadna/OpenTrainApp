package com.opentrain.app.controller;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.Stop;
import com.opentrain.app.model.Trip;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by noam on 15/11/15.
 */
public class TripMatcher {

    public static void matchTrip() {

        ArrayList<Trip> todaysTrips = MainModel.getInstance().getTrips();
        ArrayList<Station> scannedStation = (ArrayList<Station>) MainModel.getInstance().getScannedStationList();
        Map<String, String> bssidToStop = MainModel.getInstance().getBssidToStopMap();


        if (todaysTrips != null && todaysTrips.size() > 0 && scannedStation != null && scannedStation.size() > 0) {

            for (int i = 0; i < todaysTrips.size(); i++) {

                Trip trip = todaysTrips.get(i);
                int match = 0;

                for (int j = 0; j < scannedStation.size(); j++) {
                    Station station = scannedStation.get(j);

                    if (station.bssids != null) {
                        String stopId = null;
                        for (String bssid : station.bssids) {
                            stopId = bssidToStop.get(bssid);
                            if (stopId != null) {
                                break;
                            }
                        }

                        if (stopId != null && trip.stopTimes != null && trip.stopTimes.size() > j) {
                            Stop stop = trip.stopTimes.get(j);
                            String sid = Integer.toString(stop.s);
                            if (sid.equals(stopId)) {
                                match++;
                            }
                        }
                    }
                }

                if (match == scannedStation.size()) {
                    Logger.log("Found match");
                    MainModel.getInstance().setMatchedTrip(trip);
                    return;
                }
            }
        }
        Logger.log("no trip match found");

    }
}
