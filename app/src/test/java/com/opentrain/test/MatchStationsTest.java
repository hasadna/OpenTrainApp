package com.opentrain.test;

import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.MatchedStation;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.Stop;
import com.opentrain.app.model.Trip;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Elina on 06/10/2015.
 * This test handles different gtfs and scanned stations matches.
 */
// TODO - handle all coments from pull request
public class MatchStationsTest {

    // The tests contain usage of stations A-G. Station G is not mapped in the server.

    private static final String BSSID_STATION_A = "bssid_station_a";
    private static final String BSSID_STATION_A_2 = "bssid_station_a2";
    private static final String BSSID_STATION_B = "bssid_station_b";
    private static final String BSSID_STATION_C = "bssid_station_c";
    private static final String BSSID_STATION_D = "bssid_station_d";
    private static final String BSSID_STATION_D_2 = "bssid_station_d2";
    private static final String BSSID_STATION_E = "bssid_station_e";
    private static final String BSSID_STATION_F = "bssid_station_f";

    public static final Set<String> SET_BSSIDS_A = new HashSet<>(Arrays.asList(BSSID_STATION_A, BSSID_STATION_A_2));
    public static final Set<String> SET_BSSIDS_B = new HashSet<>(Arrays.asList(BSSID_STATION_B));
    public static final Set<String> SET_BSSIDS_C = new HashSet<>(Arrays.asList(BSSID_STATION_C));
    public static final Set<String> SET_BSSIDS_D = new HashSet<>(Arrays.asList(BSSID_STATION_D, BSSID_STATION_D_2));
    public static final Set<String> SET_BSSIDS_E = new HashSet<>(Arrays.asList(BSSID_STATION_E));
    public static final Set<String> SET_BSSIDS_F = new HashSet<>(Arrays.asList(BSSID_STATION_F));

    private static final String STOP_ID_STATION_A = "37358";
    private static final String STOP_ID_STATION_B = "37350";
    private static final String STOP_ID_STATION_C = "37292";
    private static final String STOP_ID_STATION_D = "37338";
    private static final String STOP_ID_STATION_E = "37336";
    private static final String STOP_ID_STATION_G = "37322";

    private static final long BASE_TIME = 1444130554980L;
    private static final long STOP_TIME_OFFSET = 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_LONG = 10 * 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_SHORT = 5 * 60 * 1000;
    private static final long OFFSET_30_SEC = 30 * 1000;

    // Initialize gtfs trips:
    private static final List<Stop> BASIC_STATION_LIST_AB = getBasicGtfsStationListAB();
    private static final List<Stop> BASIC_STATION_LIST_ABCDE = getBasicGtfsStationListABCDE();
    private static final List<Stop> BASIC_STATION_LIST_ABC = getBasicGtfsStationListABC();
    private static final List<Stop> BASIC_STATION_LIST_ABCGE_UNMAPPED = getBasicGtfsStationListABCGEUNMAPPED();

    private static List<Stop> getBasicGtfsStationListAB() {
        List<Stop> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, 1, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, 2, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        return basicStationList;
    }

    private static List<Stop> getBasicGtfsStationListABCDE() {
        List<Stop> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, 1, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, 2, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addGtfsStation(basicStationList, 3, STOP_ID_STATION_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, 4, STOP_ID_STATION_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, 5, STOP_ID_STATION_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + (BETWEEN_STOPS_OFFSET_SHORT*2));
        return basicStationList;
    }

    private static List<Stop> getBasicGtfsStationListABC() {
        List<Stop> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, 1, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, 2, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addGtfsStation(basicStationList, 3, STOP_ID_STATION_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        return basicStationList;
    }

    // Station G is unmapped.
    private static List<Stop> getBasicGtfsStationListABCGEUNMAPPED() {
        List<Stop> basicStationList = new ArrayList<>();
        addGtfsStation(basicStationList, 1, STOP_ID_STATION_A, BASE_TIME);
        addGtfsStation(basicStationList, 2, STOP_ID_STATION_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addGtfsStation(basicStationList, 3, STOP_ID_STATION_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, 4, STOP_ID_STATION_G, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + BETWEEN_STOPS_OFFSET_SHORT);
        addGtfsStation(basicStationList, 5, STOP_ID_STATION_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT * 2));
        return basicStationList;
    }

    private static void addGtfsStation(List<Stop> stationList, int index, String stationName, long enterUnixTimeMs) {
        int id = Integer.parseInt(stationName);
        stationList.add(new Stop(index, id, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET));
    }

    private static void addScannedStation(List<Station> stationList, Set<String> bssids, long enterUnixTimeMs) {
        stationList.add(new Station(bssids, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET));
    }

    private static void addMatchedStations(List<MatchedStation> stationList, Station scanned, Stop gtfs) {
        stationList.add(new MatchedStation(scanned, gtfs));
    }

    private void addGtfsStation(int index, String stationName, long enterUnixTimeMs) {
        addGtfsStation(gtfsStations, index, stationName, enterUnixTimeMs);
    }

    private void addScannedStation(Set<String> bssids, long enterUnixTimeMs) {
        addScannedStation(scannedStations, bssids, enterUnixTimeMs);
    }

    private void addMatchedStations(Station scanned, Stop gtfs) {
        addMatchedStations(expectedMatchedStations, scanned, gtfs);
    }

    private MainModel mainModel;

    @Before
    public void setUp() {
        gtfsStations = new ArrayList<Stop>();
        scannedStations = new ArrayList<Station>();
        expectedMatchedStations = new ArrayList<MatchedStation>();

        BssidMap bssidMap = new BssidMap();
        bssidMap.put(BSSID_STATION_A, STOP_ID_STATION_A);
        bssidMap.put(BSSID_STATION_A_2, STOP_ID_STATION_A);
        bssidMap.put(BSSID_STATION_B, STOP_ID_STATION_B);
        bssidMap.put(BSSID_STATION_C, STOP_ID_STATION_C);
        bssidMap.put(BSSID_STATION_D, STOP_ID_STATION_D);
        bssidMap.put(BSSID_STATION_D_2, STOP_ID_STATION_D);
        bssidMap.put(BSSID_STATION_E, STOP_ID_STATION_E);
        MainModel.reset();
        mainModel = MainModel.getInstance();
        MainController.execute(new UpdateBssidMapAction(bssidMap));
    }

    /*
     * Test parameters that control the initial state.
     */
    // Input scanned stations.
    public List<Station> scannedStations;

    // Input Gtfs stations.
    public List<Stop> gtfsStations;

    // Output matched stations list (scanned + gtfs data).
    public List<MatchedStation> expectedMatchedStations;


    private void runAndCheckResult() {
        Trip trip = new Trip("1", gtfsStations);
        List<MatchedStation> matchedStations = mainModel.alignScannedTripToGtfsTrip(scannedStations, trip);
        assertEquals(expectedMatchedStations.size(), matchedStations.size());
        for (int i = 0; i < expectedMatchedStations.size(); i++) {
            assertEquals(expectedMatchedStations.get(i).scannedStation, matchedStations.get(i).scannedStation);
            assertEquals(expectedMatchedStations.get(i).stop, matchedStations.get(i).stop);
        }
    }

    @Test
    public void testStationsABScannedAndABGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_AB;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        // Set the expected matched stations
        // Scanned: A,B  Gtfs: A,B
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        runAndCheckResult();
    }

    @Test
    public void testStationsABScannedAndABGtfsBLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_AB;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*5));
        // Set the expected matched stations
        // Scanned: A,B  Gtfs: A,B
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsABScannedAndABCDEGtfsBLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*4));
        // Set the expected matched stations
        // Scanned: A,B  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsBDScannedAndABCDEGtfsDLate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG*2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*4));
        // Set the expected matched stations
        // Scanned: B,D  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(1));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(3));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsCDScannedAndABCGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABC;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT);
        // Set the expected matched stations
        // Scanned: C,D  Gtfs: A,B,C
        addMatchedStations(scannedStations.get(0), gtfsStations.get(2));
        addMatchedStations(scannedStations.get(1), null);

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsDEScannedAndABCDEGtfsDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*7));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT*2) + (OFFSET_30_SEC*11));
        // Set the expected matched stations
        // Scanned: D,E  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(3));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(4));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsBScannedAndABCGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABC;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        // Set the expected matched stations
        // Scanned: B  Gtfs: A,B,C
        addMatchedStations(scannedStations.get(0), gtfsStations.get(1));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsABDEScannedAndABCDEGtfsBDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*3));
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*8));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT*2) + (OFFSET_30_SEC*10));
        // Set the expected matched stations
        // Scanned: A,B,D,E  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));
        addMatchedStations(scannedStations.get(2), gtfsStations.get(3));
        addMatchedStations(scannedStations.get(3), gtfsStations.get(4));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsCDEScannedAndABCDEGtfsCDELate() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*14));
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*9));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT * 2) + (OFFSET_30_SEC * 8));
        // Set the expected matched stations
        // Scanned: C,D,E  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(2));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(3));
        addMatchedStations(scannedStations.get(2), gtfsStations.get(4));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsBCDScannedAndABCDEGtfsBEarly() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG - (OFFSET_30_SEC*3));
        addScannedStation(SET_BSSIDS_C, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG + BETWEEN_STOPS_OFFSET_SHORT);
        addScannedStation(SET_BSSIDS_D, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + BETWEEN_STOPS_OFFSET_SHORT);
        // Set the expected matched stations
        // Scanned: B,C,D  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(1));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(2));
        addMatchedStations(scannedStations.get(2), gtfsStations.get(3));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsABFEScannedAndABCDEGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCDE;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addScannedStation(SET_BSSIDS_F, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 3));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT * 2));
        // Set the expected matched stations
        // Scanned: A,B,F-unmapped,E  Gtfs: A,B,C,D,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));
        addMatchedStations(scannedStations.get(2), null);
        addMatchedStations(scannedStations.get(3), gtfsStations.get(4));

        // Run test and check equality
        runAndCheckResult();

    }

    @Test
    public void testStationsABFEScannedAndABCUNKNOWNEGtfsOnTime() {

        // Set Gtfs stations
        gtfsStations = BASIC_STATION_LIST_ABCGE_UNMAPPED;
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME);
        addScannedStation(SET_BSSIDS_B, BASE_TIME + BETWEEN_STOPS_OFFSET_LONG);
        addScannedStation(SET_BSSIDS_F, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 3));
        addScannedStation(SET_BSSIDS_E, BASE_TIME + (BETWEEN_STOPS_OFFSET_LONG * 2) + (BETWEEN_STOPS_OFFSET_SHORT * 2));
        // Set the expected matched stations
        // Scanned: A,B,F-unmapped,E  Gtfs: A,B,C,G-unmapped,E
        addMatchedStations(scannedStations.get(0), gtfsStations.get(0));
        addMatchedStations(scannedStations.get(1), gtfsStations.get(1));
        addMatchedStations(scannedStations.get(2), null);
        addMatchedStations(scannedStations.get(3), gtfsStations.get(4));

        // Run test and check equality
        runAndCheckResult();

    }
}
