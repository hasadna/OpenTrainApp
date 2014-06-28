package il.org.hasadna.opentrain.tests.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import il.org.hasadna.opentrain.service.LocationScanner;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockLocationScanner extends LocationScanner {

    public static final String LOCATION_PROVIDER = "fused";
    public static final String ACTION_MOCK_LOCATION_DONE = "MockLocationScanner";

    public MockLocationScanner(Context context) {
        super(context);
    }

    @Override
    public void onConnected() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 7; i++) {
                    if (mLocationClient.isConnected()) {
                        mLocationClient.setMockMode(true);
                        Location mockLocation = new Location(LOCATION_PROVIDER);
                        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        mockLocation.setTime(System.currentTimeMillis());
                        mockLocation.setAccuracy(WAYPOINTS_ACCURACY[i]);
                        mockLocation.setLatitude(WAYPOINTS_LAT[i]);
                        mockLocation.setLongitude(WAYPOINTS_LNG[i]);
                        mLocationClient.setMockLocation(mockLocation);
                        reportNewLocationReceived(mockLocation);
                        try {
                            Thread.sleep(1000 * 15);
                        } catch (Exception e) {

                        }
                    }
                }
                reportDone();
            }
        }).start();
    }

    public static final double[] WAYPOINTS_LAT = {
            32.083696,
            32.082224,
            32.080051,
            32.076178,
            32.073233,
            32.071542,
            32.067160,
            32.064828,

    };

    // An array of longitudes for constructing il.org.hasadna.opentrain.test data
    public static final double[] WAYPOINTS_LNG = {
            34.798299,
            34.797977,
            34.797290,
            34.794114,
            34.793170,
            34.793127,
            34.792183,
            34.790086,
    };

    // An array of accuracy values for constructing il.org.hasadna.opentrain.test data
    public static final float[] WAYPOINTS_ACCURACY = {
            3.0f,
            3.12f,
            3.5f,
            3.7f,
            3.12f,
            3.0f,
            3.12f,
            3.7f
    };

    private void reportDone() {
        Intent i = new Intent(ACTION_MOCK_LOCATION_DONE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }
}
