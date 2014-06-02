package test;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import il.org.hasadna.opentrain.LocationScanner;
import il.org.hasadna.opentrain.Scanner;
import il.org.hasadna.opentrain.ScannerService;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockLocationScanner implements Scanner.IScanner {

    public static final String LOCATION_PROVIDER = "fused";

    private LocationClient mLocationClient;
    private Context mContext;

    public MockLocationScanner(Context context) {
        this.mContext = context;
        mLocationClient = new LocationClient(mContext, connectionCallbacks, onConnectionFailedListener);
    }

    @Override
    public void start() {
        mLocationClient.connect();
    }

    @Override
    public void stop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.setMockMode(false);
            mLocationClient.disconnect();
        }
    }

    private GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < 7; i++) {
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
                            Thread.sleep(5000);
                        } catch (Exception e) {

                        }
                    }
                    if(onDoneListener!=null){
                        onDoneListener.onDone();
                    }
                }
            }).start();
        }

        @Override
        public void onDisconnected() {

        }
    };

    private GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    };

    private void reportNewLocationReceived(Location location) {
        Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
        i.putExtra(Intent.EXTRA_SUBJECT, LocationScanner.LOCATION_SCANNER_EXTRA_SUBJECT);
        i.putExtra(LocationScanner.LOCATION_SCANNER_ARG_LOCATION, location);
        i.putExtra("time", System.currentTimeMillis());
        mContext.sendBroadcast(i);

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

    // An array of longitudes for constructing test data
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

    // An array of accuracy values for constructing test data
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

    public void setLocationListener(OnDoneListener listener) {
        this.onDoneListener = listener;
    }

    public interface OnDoneListener {
        public void onDone();
    }

    private OnDoneListener onDoneListener;
}
