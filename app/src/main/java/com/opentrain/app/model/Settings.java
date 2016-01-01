package com.opentrain.app.model;

/**
 * Created by noam on 30/05/15.
 */
public class Settings {

    public static final long SCAN_INTERVAL_TEST = 300;
    public static final long SCAN_INTERVAL_TRAIN = 15000;
    public static final long SCAN_KEEPALIVE_TRAIN = 60000;
    public static final long SCAN_KEEPALIVE_BETWEEN_STATIONS = 5 * 60 * 1000;
    public static final String STATION_SSID_SIMULATOR = "WiredSSID";
    public static final String STATION_SSID_TRAIN = "S-ISRAEL-RAILWAYS";
    public static final String url_get_map_from_server = "http://gtfs.otrain.org/api/data/bssids/";
    public static final String url_add_map_to_server =   "http://gtfs.otrain.org/api/data/bssids/add/";
    public static final String url_get_stops_from_server = "http://gtfs.otrain.org/api/gtfs/stops/?format=json";
    public static final String url_get_trips_from_server = "http://gtfs.otrain.org/api/gtfs/trips/date/today/?format=json";
    public static final String TEST_TIME_BASE = "08:38:55";

    public static long SCAN_INTERVAL = SCAN_INTERVAL_TRAIN;
    public static String stationSSID = STATION_SSID_TRAIN;
    public static long SCAN_KEEPALIVE = SCAN_KEEPALIVE_TRAIN;

    public static void setTestSettings() {
        SCAN_INTERVAL = SCAN_INTERVAL_TEST;
    }

    public static void setDefaultettings() {
        SCAN_INTERVAL = SCAN_INTERVAL_TRAIN;
        SCAN_KEEPALIVE = SCAN_KEEPALIVE_TRAIN;
        stationSSID = STATION_SSID_TRAIN;
    }

    public static void setSettings(String israelRailwaysStationBssid, long stationKeepaliveMs) {
        SCAN_KEEPALIVE = stationKeepaliveMs;
        stationSSID = israelRailwaysStationBssid;
    }

}
