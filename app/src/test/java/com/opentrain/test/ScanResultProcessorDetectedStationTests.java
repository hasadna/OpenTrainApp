package com.opentrain.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.ScanResultProcessor;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;

/**
 * Created by Elina on 9/24/2015.
 */
/*
 * This test handles different initial states and different ScanResult inputs.
 * All tests start at the same start time and with the same bssid map.
 */
@RunWith(Parameterized.class)
public class ScanResultProcessorDetectedStationTests {
    public enum StationState {
        NOW_IN_INITIAL_STATION, WAS_IN_INITIAL_STATION, NO_PREVIOUS_STATION
    }
    public enum ScanResultStation {
        INITIAL_STATION, DIFFERENT_STATION
    }

    private static final String SSID = "S-ISRAEL-RAILWAYS";
    private static final int KEEPALIVE_MS = 60 * 1000;
    private static final String BSSID_INITIAL_STATION = "bssid_initial_station";
    private static final String BSSID_DIFFERENT_STATION = "bssid_different_station";
    private static final String BSSID_UNMAPPED = "bssid_unmapped";
    private static final String STOP_ID_INITIAL_STATION = "stop_id_initial_station";
    private static final String STOP_ID_DIFFERENT_STATION = "stop_id_different_station";
    private static final long START_TIME_UNIX_MS = 0;
    private static final long WITHIN_KEEPALIVE_UNIX_MS = START_TIME_UNIX_MS + KEEPALIVE_MS/2;
    private static final long NOT_WITHIN_KEEPALIVE_UNIX_MS = START_TIME_UNIX_MS + KEEPALIVE_MS*2;

    private MainModel mainModel;
    ScanResultProcessor.Settings settings;

    @Parameters(name = "{index}: StationState={0}, ScanResultStation={1}, WithUnmappedBssid={2}, ScanContradiction={3}, WithinKeepalive={4}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> result = new ArrayList<>();

        for (StationState stationState : StationState.values()) {
            for (ScanResultStation scanResultStation : ScanResultStation.values()) {
                for (int withUnmappedBssid = 0; withUnmappedBssid < 2; withUnmappedBssid++) {
                    for (int scanContradiction = 0; scanContradiction < 2; scanContradiction++) {
                        for (int withinKeepalive = 0; withinKeepalive < 2; withinKeepalive++) {
                            if (stationState == StationState.NO_PREVIOUS_STATION &&
                                    scanResultStation == ScanResultStation.DIFFERENT_STATION) {
                                // If we weren't in a previous station then no need to check both INITIAL_STATION
                                // and DIFFERENT_STATION.
                                continue;
                            }
                            result.add(new Object[] { stationState, scanResultStation, withUnmappedBssid != 0,
                                    scanContradiction != 0, withinKeepalive != 0 });
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * Test parameters that control the initial state.
     */
    @Parameter(value = 0)
    // Controls the station in the state and whether we're inStation.
    public StationState stationState;

    /*
     * Test parameters that control the ScanResult.
     */
    @Parameter(value = 1)
    // The previous station.
    public ScanResultStation scanResultStation;

    @Parameter(value = 2)
    // ScanResult contains an unmapped bssid.
    public boolean withUnmappedBssid;

    @Parameter(value = 3)
    // ScanResult contains two ScanResultItems who's bssids map to different stations.
    public boolean scanContradiction;

    @Parameter(value = 4)
    // ScanResult time is within the keepalive timeframe from the last station.
    public boolean withinKeepalive;

    // Get ScanResult according to test parameters
    WifiScanResult getScan(boolean withUnmappedBssid, boolean scanContradiction,
                       boolean withinKeepalive, ScanResultStation scanResultStation) {
        long timeUnixMs =
                (withinKeepalive ? WITHIN_KEEPALIVE_UNIX_MS : NOT_WITHIN_KEEPALIVE_UNIX_MS);
        ArrayList<WifiScanResultItem> scanResultItems = new ArrayList<>();
        if (withUnmappedBssid) {
            scanResultItems.add(new WifiScanResultItem(BSSID_UNMAPPED, SSID));
        }
        if (scanContradiction) {
            scanResultItems.add(new WifiScanResultItem(BSSID_INITIAL_STATION, SSID));
            scanResultItems.add(new WifiScanResultItem(BSSID_DIFFERENT_STATION, SSID));
        } else if (scanResultStation == ScanResultStation.INITIAL_STATION) {
            scanResultItems.add(new WifiScanResultItem(BSSID_INITIAL_STATION, SSID));
        } else {  // For both SAME and NONE PreviousStation
            scanResultItems.add(new WifiScanResultItem(BSSID_DIFFERENT_STATION, SSID));
        }
        return new WifiScanResult(scanResultItems, timeUnixMs);
    }

    @Before
    public void setUp() {
        settings = new ScanResultProcessor.Settings(SSID, KEEPALIVE_MS);
        MainModel.reset();
        mainModel = MainModel.getInstance();
    }

    private Set<String> getSet(String value) {
        return new HashSet<String>(Arrays.asList(value));
    }

    @Test
    public void parametrizedTest() {
        // Set state
        HashMap<String, String> bssidMap = new HashMap<>();
        bssidMap.put(BSSID_INITIAL_STATION, STOP_ID_INITIAL_STATION);
        bssidMap.put(BSSID_DIFFERENT_STATION, STOP_ID_DIFFERENT_STATION);
        mainModel.setBssidMap(bssidMap);
        if (stationState != StationState.NO_PREVIOUS_STATION) {
            mainModel.getScannedStationList().add(new Station(getSet(BSSID_INITIAL_STATION), /*STOP_ID_INITIAL_STATION,*/ START_TIME_UNIX_MS));
        }
        mainModel.setInStation(stationState == StationState.NOW_IN_INITIAL_STATION);

        // Set scan result
        WifiScanResult scanResult = getScan(withUnmappedBssid, scanContradiction, withinKeepalive,
                scanResultStation);

        // Run test
        ScanResultProcessor.process(mainModel, scanResult, settings);

        // Evaluate test result
        WifiScanResultItem firstScanItem = scanResult.wifiScanResultItems.get(0);
        if (withUnmappedBssid || scanContradiction) {
            // Create new unknown station
            assertEquals(mainModel.isInStation(), true);
            Station station;
            if (stationState == StationState.NO_PREVIOUS_STATION) {
                assertEquals(mainModel.getScannedStationList().size(), 1);
                station = mainModel.getScannedStationList().get(0);
            } else {
                assertEquals(mainModel.getScannedStationList().size(), 2);
                station = mainModel.getScannedStationList().get(1);
            }
            assertEquals(Station.UNKNOWN_STOP_ID, station.getId());
            assertEquals(scanResult.unixTimeMs, station.enterUnixTimeMs);
            assertEquals(scanResult.unixTimeMs, station.lastSeenUnixTimeMs);
            assertEquals(null, station.exitUnixTimeMs);
            assertEquals(scanResult.getBssids(), station.bssids);
        } else if (!withinKeepalive) {
            // Create new station
            assertEquals(mainModel.isInStation(), true);
            Station station;
            if (stationState == StationState.NO_PREVIOUS_STATION) {
                assertEquals(mainModel.getScannedStationList().size(), 1);
                station = mainModel.getScannedStationList().get(0);
            } else {
                assertEquals(mainModel.getScannedStationList().size(), 2);
                station = mainModel.getScannedStationList().get(1);
            }
            assertEquals(bssidMap.get(firstScanItem.BSSID), station.getId());
            assertEquals(NOT_WITHIN_KEEPALIVE_UNIX_MS, station.enterUnixTimeMs);
            assertEquals(NOT_WITHIN_KEEPALIVE_UNIX_MS, station.lastSeenUnixTimeMs);
            assertEquals(scanResult.getBssids(), station.bssids);
        } else {
            // At this point, withUnmappedBssid and scanContradiction are false and withinKeepalive
            // is true. That means that we have a consensus station and are within keepalive.
            if (stationState == StationState.NO_PREVIOUS_STATION) {
                // Create new station
                assertEquals(true, mainModel.isInStation());
                assertEquals(1, mainModel.getScannedStationList().size());
                Station station = mainModel.getScannedStationList().get(0);
                assertEquals(STOP_ID_INITIAL_STATION, station.getId());
                assertEquals(WITHIN_KEEPALIVE_UNIX_MS, station.enterUnixTimeMs);
                assertEquals(WITHIN_KEEPALIVE_UNIX_MS, station.lastSeenUnixTimeMs);
                assertEquals(null, station.exitUnixTimeMs);
                assertEquals(scanResult.getBssids(), station.bssids);
            } else if (scanResultStation == ScanResultStation.INITIAL_STATION) {
                // Extend current time
                assertEquals(true, mainModel.isInStation());
                assertEquals(1, mainModel.getScannedStationList().size());
                Station station = mainModel.getScannedStationList().get(0);
                assertEquals(STOP_ID_INITIAL_STATION, station.getId());
                assertEquals(START_TIME_UNIX_MS, station.enterUnixTimeMs);
                assertEquals(WITHIN_KEEPALIVE_UNIX_MS, station.lastSeenUnixTimeMs);
                assertEquals(null, station.exitUnixTimeMs);
                // We don't modify the bssids after creating the station, so they should remain unchanged.
                assertEquals(getSet(BSSID_INITIAL_STATION), station.bssids);
            } else if (scanResultStation == ScanResultStation.DIFFERENT_STATION) {
                // Create new station
                assertEquals(true, mainModel.isInStation());
                assertEquals(2, mainModel.getScannedStationList().size());
                Station station = mainModel.getScannedStationList().get(1);
                assertEquals(STOP_ID_DIFFERENT_STATION, station.getId());
                assertEquals(WITHIN_KEEPALIVE_UNIX_MS, station.enterUnixTimeMs);
                assertEquals(WITHIN_KEEPALIVE_UNIX_MS, station.lastSeenUnixTimeMs);
                assertEquals(null, station.exitUnixTimeMs);
                assertEquals(scanResult.getBssids(), station.bssids);
            }
        }
    }
}
