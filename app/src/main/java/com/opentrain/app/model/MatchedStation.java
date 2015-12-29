package com.opentrain.app.model;

/**
 * Created by Elina on 06/10/2015.
 */
public class MatchedStation {
    public Station scannedStation;
    public Stop stop;

    public MatchedStation(Station scannedStation, Stop stop) {
        this.stop = stop;
        this.scannedStation = scannedStation;
    }
}
