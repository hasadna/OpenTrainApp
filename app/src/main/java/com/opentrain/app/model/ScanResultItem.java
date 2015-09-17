package com.opentrain.app.model;

/**
 * Created by noam on 29/05/15.
 */
public class ScanResultItem {

    public final String BSSID;
    public final String SSID;


    public ScanResultItem(String BSSID, String SSID) {
        this.BSSID = BSSID;
        this.SSID = SSID;
    }

    public String toString() {
        return "BSSID: " + BSSID + " ,SSID: " + SSID;
    }
}
