package il.org.hasadna.opentrain.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import il.org.hasadna.opentrain.application.MainApplication;
import il.org.hasadna.opentrain.application.preferences.Prefs;

/**
 * Created by Noam.m on 3/6/14.
 */
public class LocationScanner {

    private static final String LOGTAG = LocationScanner.class.getName();

    public static final String LOCATION_SCANNER_EXTRA_SUBJECT = "LocationScanner";
    public static final String LOCATION_SCANNER_ARG_LOCATION = "il.org.hasadna.opentrain.LocationScanner.location";

    protected LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    protected Context mContext;
    private Prefs mPrefs;
    private Location mLastLocation;
    private long mLastLocationTime;

    public LocationScanner(Context context) {
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        mPrefs = Prefs.getInstance(context);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(mPrefs.LOCATION_API_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(mPrefs.LOCATION_API_FAST_CEILING_INTERVAL);
        mLocationClient = new LocationClient(context, connectionCallbacks, onConnectionFailedListener);
    }

    public void start() {
        mLocationClient.connect();
    }

    public void stop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(locationListener);
        }
        mLocationClient.disconnect();
    }

    private GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            LocationScanner.this.onConnected();
        }

        @Override
        public void onDisconnected() {

        }
    };

    public void onConnected() {
        reportLastLocation();
        mLocationClient.requestLocationUpdates(mLocationRequest, locationListener);
    }

    private GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (connectionResult != null) {
                Log.d(LOGTAG, connectionResult.toString());
            }
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            reportNewLocationReceived(location);
            trackLocation(location);
        }
    };

    private void reportLastLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            Location lastKnownLocation = mLocationClient.getLastLocation();
            if (lastKnownLocation != null) {
                reportNewLocationReceived(lastKnownLocation);
            }
        }
    }

    protected void reportNewLocationReceived(Location location) {
        Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
        i.putExtra(Intent.EXTRA_SUBJECT, LOCATION_SCANNER_EXTRA_SUBJECT);
        i.putExtra(LOCATION_SCANNER_ARG_LOCATION, location);
        i.putExtra("time", System.currentTimeMillis());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    //check if two points are far away from each other and if so - report.
    private void trackLocation(Location location) {
        if (location == null) {
            return;
        }
        if (mLastLocation == null) {
            mLastLocation = location;
            mLastLocationTime = System.currentTimeMillis();
        } else {
            float distance = mLastLocation.distanceTo(location);
            long interval = System.currentTimeMillis() - mLastLocationTime;
            if (distance > 2000 && interval < 5000) {
                String reportString = "GPS error indication : " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + " and " + location.getLatitude() + "," + location.getLongitude();
                ((MainApplication) mContext.getApplicationContext()).trackEvent("location", reportString);
            }
            mLastLocation = location;
            mLastLocationTime = System.currentTimeMillis();
        }
    }
}
