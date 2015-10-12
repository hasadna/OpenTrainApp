package com.opentrain.test;

import static org.junit.Assert.*;

import com.opentrain.app.model.GtfsStation;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.MatchedStation;
import com.opentrain.app.model.Station;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Elina on 10/6/2015.
 * This test handles different gtfs and scanned stations matches.
 */
public class MatchStationsTest {

    private static final String BSSID_STATION_A = "bssid_station_a";
    private static final String BSSID_STATION_A_2 = "bssid_station_a2";
    private static final String BSSID_STATION_B = "bssid_station_b";
    private static final String BSSID_STATION_C = "bssid_station_c";
    private static final String BSSID_STATION_D = "bssid_station_d";
    private static final String BSSID_STATION_D_2 = "bssid_station_d2";
    private static final String BSSID_STATION_E = "bssid_station_e";

    public static final String[] BSSIDS_A = new String[] { BSSID_STATION_A, BSSID_STATION_A_2 };
    public static final String[] BSSIDS_B = new String[] { BSSID_STATION_B };
    public static final String[] BSSIDS_C = new String[] { BSSID_STATION_C };
    public static final String[] BSSIDS_D = new String[] { BSSID_STATION_D, BSSID_STATION_D_2 };
    public static final String[] BSSIDS_E = new String[] { BSSID_STATION_E };

    public static final Set<String> SET_BSSIDS_A = new HashSet<String>(Arrays.asList(BSSIDS_A));
    public static final Set<String> SET_BSSIDS_B = new HashSet<String>(Arrays.asList(BSSIDS_B));
    public static final Set<String> SET_BSSIDS_C = new HashSet<String>(Arrays.asList(BSSIDS_C));
    public static final Set<String> SET_BSSIDS_D = new HashSet<String>(Arrays.asList(BSSIDS_D));
    public static final Set<String> SET_BSSIDS_E = new HashSet<String>(Arrays.asList(BSSIDS_E));

    private static final String STOP_ID_STATION_A = "stop_id_station_a";
    private static final String STOP_ID_STATION_B = "stop_id_station_b";
    private static final String STOP_ID_STATION_C = "stop_id_station_c";
    private static final String STOP_ID_STATION_D = "stop_id_station_d";
    private static final String STOP_ID_STATION_E = "stop_id_station_e";

    private static final long BASE_TIME = 1444130554980L;
    private static final long STOP_TIME_OFFSET = 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_LONG = 10 * 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_SHORT = 5 * 60 * 1000;
    private static final long OFFSET_30_SEC = 30 * 1000;

    // Initialize gtfs trips:
    private static final List<GtfsStation> BASIC_STATION_LIST_1 = getBasicGtfsStationList1();
    private static final List<GtfsStation> BASIC_STATION_LIST_2 = getBasicGtfsStationList2();
    private static final List<GtfsStation> BASIC_STATION_LIST_3 = getBasicGtfsStationList3();

    private static List<GtfsStation> getBasicGtfsStationList1() {
        List<GtfsStation> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        return basicStationList;
    }

    private static List<GtfsStation> getBasicGtfsStationList2() {
        List<GtfsStation> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addGtfsStation(basicStationList, STOP_ID_STATION_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, STOP_ID_STATION_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, STOP_ID_STATION_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + (BETWEEN_STOPS_OFFSET_SHORT*2));
        return basicStationList;
    }

    private static List<GtfsStation> getBasicGtfsStationList3() {
        List<GtfsStation> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addGtfsStation(basicStationList, STOP_ID_STATION_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        return basicStationList;
    }

    private static void addGtfsStation(List<GtfsStation> stationList, String stationName, long enterUnixTimeMs) {
        stationList.add(new GtfsStation(stationName, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET));
    }

    private static void addScannedStation(List<Station> stationList, Set<String> bssids, long enterUnixTimeMs) {
        stationList.add(new Station(bssids, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET));
    }

    private static void addMatchedStation(List<MatchedStation> stationList, Station scanned, GtfsStation gtfs) {
        stationList.add(new MatchedStation(scanned, gtfs));
    }

    private void addGtfsStation(String stationName, long enterUnixTimeMs) {
        addGtfsStation(gtfsStations, stationName, enterUnixTimeMs);
    }

    private void addScannedStation(Set<String> bssids, long enterUnixTimeMs) {
        addScannedStation(scannedStations, bssids, enterUnixTimeMs);
    }

    private void addMatchedStation(Station scanned, GtfsStation gtfs) {
        addMatchedStation(matchedStations, scanned, gtfs);
    }

    private MainModel mainModel;

    @Before
    public void setUp() {
        gtfsStations = new ArrayList<GtfsStation>();
        scannedStations = new ArrayList<Station>();
        matchedStations = new ArrayList<MatchedStation>();

        HashMap<String, String> bssidMap = new HashMap<>();
        bssidMap.put(BSSID_STATION_A, STOP_ID_STATION_A);
        bssidMap.put(BSSID_STATION_A_2, STOP_ID_STATION_A);
        bssidMap.put(BSSID_STATION_B, STOP_ID_STATION_B);
        bssidMap.put(BSSID_STATION_C, STOP_ID_STATION_C);
        bssidMap.put(BSSID_STATION_D, STOP_ID_STATION_D);
        bssidMap.put(BSSID_STATION_D_2, STOP_ID_STATION_D);
        bssidMap.put(BSSID_STATION_E, STOP_ID_STATION_E);
        MainModel.reset();
        mainModel = MainModel.getInstance();
        mainModel.setBssidMap(bssidMap);
    }

    /*
     * Test parameters that control the initial state.
     */
    // Input scanned stations.
    public List<Station> scannedStations;

    // Input Gtfs stations.
    public List<GtfsStation> gtfsStations;

    // Output matched stations list (scanned + gtfs data).
    public List<MatchedStation> matchedStations;


    private void checkResultEquality() {
        List<MatchedStation> matchedFromCode = mainModel.alignScannedTripToGtfsTrip(scannedStations, gtfsStations);
        assertEquals(matchedStations.size(), matchedFromCode.size());
        for (int i = 0; i < matchedStations.size(); i++) {
            assertEquals(matchedStations.get(i).scannedStation, matchedFromCode.get(i).scannedStation);
            assertEquals(matchedStations.get(i).gtfsStation, matchedFromCode.get(i).gtfsStation);
        }
    }

    @Test
    public void testStationsABScannedAndABGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_1;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        checkResultEquality();
    }

    @Test
    public void testStationsABScannedAndABGtfsBLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_1;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*5));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsABScannedAndABCDEGtfsBLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*4));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsBDScannedAndABCDEGtfsDLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*4));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(1));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(3));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsCDScannedAndABCGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_3;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT);
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(2));
        addMatchedStation(scannedStations.get(1), null);

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsDEScannedAndABCDEGtfsDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*7));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT*2) + (OFFSET_30_SEC*11));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(3));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(4));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsBScannedAndABCGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_3;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(1));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsABDEScannedAndABCDEGtfsBDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*3));
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*8));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT*2) + (OFFSET_30_SEC*10));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(1));
        addMatchedStation(scannedStations.get(2), gtfsStations.get(3));
        addMatchedStation(scannedStations.get(3), gtfsStations.get(4));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsCDEScannedAndABCDEGtfsCDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*14));
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*9));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT * 2) + (OFFSET_30_SEC * 8));
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(2));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(3));
        addMatchedStation(scannedStations.get(2), gtfsStations.get(4));

        // Run test and check equality
        checkResultEquality();

    }

    @Test
    public void testStationsBCDScannedAndABCDEGtfsBEarly() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_2;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG - (OFFSET_30_SEC*3));
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT);
        // Set the expected matched stations
        addMatchedStation(scannedStations.get(0), gtfsStations.get(1));
        addMatchedStation(scannedStations.get(1), gtfsStations.get(2));
        addMatchedStation(scannedStations.get(2), gtfsStations.get(3));

        // Run test and check equality
        checkResultEquality();

    }
}
