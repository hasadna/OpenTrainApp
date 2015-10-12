package com.opentrain.app.model;

/**
 * Created by Elina on 10/6/2015.
 */
public class MatchedStation {
    public Station scannedStation;
    public GtfsStation gtfsStation;

    public MatchedStation(Station scannedStation, GtfsStation gtfsStation) {
        this.gtfsStation = gtfsStation;
        this.scannedStation = scannedStation;
    }
}
