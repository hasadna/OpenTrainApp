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

import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.controller.ScanResultProcessor;
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
public class ScanResultProcessorNoDetectedStationTests {
    public enum StationState {
        NOW_IN_INITIAL_STATION, WAS_IN_INITIAL_STATION, NO_PREVIOUS_STATION
    }

    private static final String SSID = "S-ISRAEL-RAILWAYS";
    private static final int KEEPALIVE_MS = 60 * 1000;
    private static final String BSSID_INITIAL_STATION = "bssid_initial_station";
    private static final String STOP_ID_INITIAL_STATION = "stop_id_initial_station";
    private static final long START_TIME_UNIX_MS = 0;
    private static final long STATION_LAST_SEEN_TIME_UNIX_MS = START_TIME_UNIX_MS + 1000 * 15;
    private static final long SCAN_RESULT_TIME_UNIX_MS = STATION_LAST_SEEN_TIME_UNIX_MS + 1000 * 15;

    private MainModel mainModel;
    ScanResultProcessor.Settings settings;

    @Parameters(name = "{index}: StationState={0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> result = new ArrayList<>();

        for (StationState stationState : StationState.values()) {
            result.add(new Object[] { stationState });
        }
        return result;
    }

    /*
     * Test parameters that control the initial state.
     */
    @Parameter(value = 0)
    // Controls the station in the state and whether we're inStation.
    public StationState stationState;

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
        BssidMap bssidMap = new BssidMap();
        bssidMap.put(BSSID_INITIAL_STATION, STOP_ID_INITIAL_STATION);
        MainController.execute(new UpdateBssidMapAction(bssidMap));
        if (stationState != StationState.NO_PREVIOUS_STATION) {
            Station station = new Station(getSet(BSSID_INITIAL_STATION), /*STOP_ID_INITIAL_STATION,*/ START_TIME_UNIX_MS);
            station.enterUnixTimeMs = START_TIME_UNIX_MS;
            station.lastSeenUnixTimeMs = STATION_LAST_SEEN_TIME_UNIX_MS;
            mainModel.getScannedStationList().add(station);
        }
        mainModel.setInStation(stationState == StationState.NOW_IN_INITIAL_STATION);

        // Set scan result
        WifiScanResult scanResult =
                new WifiScanResult(SCAN_RESULT_TIME_UNIX_MS, new ArrayList<WifiScanResultItem>());

        // Run test
        ScanResultProcessor.process(mainModel, scanResult, settings);

        // Test result
        if (stationState != StationState.NO_PREVIOUS_STATION) {
            assertEquals(false, mainModel.isInStation());
            assertEquals(1, mainModel.getScannedStationList().size());
            Station station = mainModel.getScannedStationList().get(0);
            assertEquals(STOP_ID_INITIAL_STATION, station.getId());
            assertEquals(START_TIME_UNIX_MS, station.enterUnixTimeMs);
            assertEquals(STATION_LAST_SEEN_TIME_UNIX_MS, station.lastSeenUnixTimeMs);
            assertEquals(STATION_LAST_SEEN_TIME_UNIX_MS, station.exitUnixTimeMs.longValue());
        } else {  // stationState == StationState.NO_PREVIOUS_STATION
            assertEquals(false, mainModel.isInStation());
            assertEquals(0, mainModel.getScannedStationList().size());
        }
    }
}
