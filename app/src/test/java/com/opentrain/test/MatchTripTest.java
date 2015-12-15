package com.opentrain.test;

import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.TripMatcher;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;
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
 * Created by Elina on 08 Dec 2015.
 * This test handles match of scanned stations and trips.
 */
public class MatchTripTest {

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

    private static final int STOP_ID_STATION_A = 37358;
    private static final int STOP_ID_STATION_B = 37350;
    private static final int STOP_ID_STATION_C = 37292;
    private static final int STOP_ID_STATION_D = 37338;
    private static final int STOP_ID_STATION_E = 37336;
    private static final int STOP_ID_STATION_F = 37322;

    private static final long BASE_TIME_MORNING_1 =     1444110554980L; // 06 Oct 2015, 8:49:14
    private static final long BASE_TIME_MORNING_2 =     1444111704980L; // 9:08:24
    private static final long BASE_TIME_MORNING_3 =     1444111901980L; // 9:11:41
    private static final long BASE_TIME_NOON =          1444122754980L; // 12:12:34
    private static final long BASE_TIME_EVENING =       1444139517980L; // 16:51:57
    private static final long STOP_TIME_OFFSET = 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_LONG = 10 * 60 * 1000;
    private static final long BETWEEN_STOPS_OFFSET_SHORT = 5 * 60 * 1000;
    private static final long OFFSET_30_SEC = 30 * 1000;

    private static Stop addStop(int id, int s, long enterUnixTimeMs) {
        return new Stop(id, s, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET);
    }

    private static Trip addTripStationsABMorning1() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_A, BASE_TIME_MORNING_1));
        stops.add(addStop(2, STOP_ID_STATION_B, BASE_TIME_MORNING_1 + BETWEEN_STOPS_OFFSET_SHORT));
        return new Trip("ABMorning1", stops);
    }
    private static Trip addTripStationsABNoon() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_A, BASE_TIME_NOON));
        stops.add(addStop(2, STOP_ID_STATION_B, BASE_TIME_NOON + BETWEEN_STOPS_OFFSET_SHORT));
        return new Trip("ABNoon", stops);
    }
    private static Trip addTripStationsABCDEMorning1() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_A, BASE_TIME_MORNING_1));
        stops.add(addStop(2, STOP_ID_STATION_B, BASE_TIME_MORNING_1 + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(3, STOP_ID_STATION_C, BASE_TIME_MORNING_1 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        stops.add(addStop(4, STOP_ID_STATION_D, BASE_TIME_MORNING_1 + BETWEEN_STOPS_OFFSET_SHORT + (BETWEEN_STOPS_OFFSET_LONG*2)));
        stops.add(addStop(5, STOP_ID_STATION_E, BASE_TIME_MORNING_1 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2)));
        return new Trip("ABCDEMorning1", stops);
    }
    private static Trip addTripStationsABCDEMorning2() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_A, BASE_TIME_MORNING_2));
        stops.add(addStop(2, STOP_ID_STATION_B, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(3, STOP_ID_STATION_C, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        stops.add(addStop(4, STOP_ID_STATION_D, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + (BETWEEN_STOPS_OFFSET_LONG*2)));
        stops.add(addStop(5, STOP_ID_STATION_E, BASE_TIME_MORNING_2 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2)));
        return new Trip("ABCDEMorning2", stops);
    }
    private static Trip addTripStationsACEFMorning3() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_A, BASE_TIME_MORNING_3));
        stops.add(addStop(2, STOP_ID_STATION_C, BASE_TIME_MORNING_3 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        stops.add(addStop(3, STOP_ID_STATION_E, BASE_TIME_MORNING_3 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2)));
        stops.add(addStop(4, STOP_ID_STATION_F, BASE_TIME_MORNING_3 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*3)));
        return new Trip("ACEFMorning3", stops);
    }
    private static Trip addTripStationsEDCBAMorning2() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_E, BASE_TIME_MORNING_2));
        stops.add(addStop(2, STOP_ID_STATION_D, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(3, STOP_ID_STATION_C, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        stops.add(addStop(4, STOP_ID_STATION_B, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + (BETWEEN_STOPS_OFFSET_LONG*2)));
        stops.add(addStop(5, STOP_ID_STATION_A, BASE_TIME_MORNING_2 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2)));
        return new Trip("EDCBAMorning2", stops);
    }
    private static Trip addTripStationsCDENoon() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_C, BASE_TIME_NOON));
        stops.add(addStop(2, STOP_ID_STATION_D, BASE_TIME_NOON + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(3, STOP_ID_STATION_E, BASE_TIME_NOON + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        return new Trip("CDENoon", stops);
    }
    private static Trip addTripStationsCDEEvening() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_C, BASE_TIME_EVENING));
        stops.add(addStop(2, STOP_ID_STATION_D, BASE_TIME_EVENING + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(3, STOP_ID_STATION_E, BASE_TIME_EVENING + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG));
        return new Trip("CDEEvening", stops);
    }
    private static Trip addTripStationsDBAMorning3() {
        List<Stop> stops = new ArrayList<>();
        stops.add(addStop(1, STOP_ID_STATION_D, BASE_TIME_MORNING_3 + BETWEEN_STOPS_OFFSET_SHORT));
        stops.add(addStop(2, STOP_ID_STATION_B, BASE_TIME_MORNING_3 + BETWEEN_STOPS_OFFSET_SHORT + (BETWEEN_STOPS_OFFSET_LONG*2)));
        stops.add(addStop(3, STOP_ID_STATION_A, BASE_TIME_MORNING_3 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2)));
        return new Trip("DBAMorning3", stops);
    }


    private static void addScannedStation(List<Station> stationList, Set<String> bssids, long enterUnixTimeMs) {
        stationList.add(new Station(bssids, enterUnixTimeMs, enterUnixTimeMs + STOP_TIME_OFFSET));
    }

    private void addScannedStation(Set<String> bssids, long enterUnixTimeMs) {
        addScannedStation(scannedStations, bssids, enterUnixTimeMs);
    }

    private MainModel mainModel;

    // Input scanned stations.
    public List<Station> scannedStations;

    // Input today planned trips.
    public List<Trip> todaysTrips;

    // Output matched stations list (scanned + gtfs data).
    public Trip expectedMatchedTrip;


    @Before
    public void setUp() {
        todaysTrips = new ArrayList<Trip>();
        todaysTrips.add(MatchTripTest.addTripStationsABMorning1());
        todaysTrips.add(MatchTripTest.addTripStationsABCDEMorning1());
        todaysTrips.add(MatchTripTest.addTripStationsABCDEMorning2());
        todaysTrips.add(MatchTripTest.addTripStationsABNoon());
        todaysTrips.add(MatchTripTest.addTripStationsACEFMorning3());
        todaysTrips.add(MatchTripTest.addTripStationsEDCBAMorning2());
        todaysTrips.add(MatchTripTest.addTripStationsCDENoon());
        todaysTrips.add(MatchTripTest.addTripStationsCDEEvening());
        todaysTrips.add(MatchTripTest.addTripStationsDBAMorning3());

        scannedStations = new ArrayList<Station>();

        BssidMap bssidMap = new BssidMap();
        bssidMap.put(BSSID_STATION_A, Integer.toString(STOP_ID_STATION_A));
        bssidMap.put(BSSID_STATION_A_2, Integer.toString(STOP_ID_STATION_A));
        bssidMap.put(BSSID_STATION_B, Integer.toString(STOP_ID_STATION_B));
        bssidMap.put(BSSID_STATION_C, Integer.toString(STOP_ID_STATION_C));
        bssidMap.put(BSSID_STATION_D, Integer.toString(STOP_ID_STATION_D));
        bssidMap.put(BSSID_STATION_D_2, Integer.toString(STOP_ID_STATION_D));
        bssidMap.put(BSSID_STATION_E, Integer.toString(STOP_ID_STATION_E));
        MainModel.reset();
        mainModel = MainModel.getInstance();
        MainController.execute(new UpdateBssidMapAction(bssidMap));

        MainModel.getInstance().setTrips(todaysTrips);
    }

    private void runAndCheckResult() {
        Trip trip = TripMatcher.matchTrip();
        assertEquals(trip.stopTimes.size(), expectedMatchedTrip.stopTimes.size());
        assertEquals(trip.id, expectedMatchedTrip.id);
        for (int i = 0; i < expectedMatchedTrip.stopTimes.size(); i++) {
            assertEquals(expectedMatchedTrip.stopTimes.get(i).arrival, trip.stopTimes.get(i).arrival);
            assertEquals(expectedMatchedTrip.stopTimes.get(i).departure, trip.stopTimes.get(i).departure);
            assertEquals(expectedMatchedTrip.stopTimes.get(i).s, trip.stopTimes.get(i).s);
            assertEquals(expectedMatchedTrip.stopTimes.get(i).index, trip.stopTimes.get(i).index);
        }
    }

    // Very simple match check A -> A, B -> B. No other stations exist.
    @Test
    public void testStationsABScannedAndABGtfsOnTime() {
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME_NOON + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME_NOON + BETWEEN_STOPS_OFFSET_SHORT + OFFSET_30_SEC);
        MainModel.getInstance().setScannedStationList(scannedStations);
        // Set the expected matched trip
        expectedMatchedTrip = MatchTripTest.addTripStationsABNoon();
        // Run test and check equality
        runAndCheckResult();
    }

    // Scanned 3 stations, and the gtfs route contains more stations
    @Test
    public void testStationsABCScannedAndABCDEGtfsOnTime() {
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME_MORNING_2 + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*2));
        addScannedStation(SET_BSSIDS_C, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*2));
        MainModel.getInstance().setScannedStationList(scannedStations);
        // Set the expected matched trip
        expectedMatchedTrip = MatchTripTest.addTripStationsABCDEMorning2();

        // Run test and check equality
        runAndCheckResult();
    }

    // Late trains cause error, and another route is chosen.
    @Test
    public void testStationsABCScannedAndABCDEGtfsLate() {
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_A, BASE_TIME_MORNING_2 + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + (OFFSET_30_SEC*5));
        addScannedStation(SET_BSSIDS_C, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + BETWEEN_STOPS_OFFSET_LONG + (OFFSET_30_SEC*7));
        MainModel.getInstance().setScannedStationList(scannedStations);
        // Set the expected matched trip
        expectedMatchedTrip = MatchTripTest.addTripStationsACEFMorning3();

        // Run test and check equality
        runAndCheckResult();
    }

    // Scanned stations do not include all stations in route.
    @Test
    public void testStationsDBAScannedAndEDCBAGtfsOnTime() {
        // Set Scanned stations
        addScannedStation(SET_BSSIDS_D, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_B, BASE_TIME_MORNING_2 + BETWEEN_STOPS_OFFSET_SHORT + (BETWEEN_STOPS_OFFSET_LONG*2) + OFFSET_30_SEC);
        addScannedStation(SET_BSSIDS_A, BASE_TIME_MORNING_2 + (BETWEEN_STOPS_OFFSET_SHORT*2) + (BETWEEN_STOPS_OFFSET_LONG*2));
        MainModel.getInstance().setScannedStationList(scannedStations);
        // Set the expected matched trip
        expectedMatchedTrip = MatchTripTest.addTripStationsEDCBAMorning2();

        // Run test and check equality
        runAndCheckResult();
    }
}
