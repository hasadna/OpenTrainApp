package il.org.hasadna.opentrain;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import il.org.hasadna.opentrain.application.MainApplication;
import il.org.hasadna.opentrain.monitoring.JsonDumper;
import il.org.hasadna.opentrain.preferences.Prefs;

/**
 * Created by Noam.m on 3/6/14.
 */
public class LocationScanner implements Scanner.IScanner{

    private static final String LOGTAG = LocationScanner.class.getName();

    public static final String LOCATION_SCANNER_EXTRA_SUBJECT = "LocationScanner";
    public static final String LOCATION_SCANNER_ARG_LOCATION = "il.org.hasadna.opentrain.LocationScanner.location";

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private Context mContext;
    JsonDumper mLogJsonLocation=null;

    private Prefs mPrefs;

    private Location mLastLocation;
    private long mLastLocationTime;

    LocationScanner(Context context) {
        init(context);
        mLogJsonLocation= new JsonDumper(context, "raw.location");
        
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

    public Location getLocation() {
        if (servicesConnected()) {
            Location currentLocation = mLocationClient.getLastLocation();
            return currentLocation;
        }
        return null;
    }

    public void start() {
        mLogJsonLocation.open();
        mLocationClient.connect();
    }

    public void stop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(locationListener);
        }
        mLocationClient.disconnect();
        mLogJsonLocation.close();
    }

    private GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            reportLastLocation();
            mLocationClient.requestLocationUpdates(mLocationRequest, locationListener);
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

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            reportNewLocationReceived(location);
            if (locationCallBack != null) {
                locationCallBack.onLocationCallBack(location);
            }
            trackLocation(location);
        }
    };

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
            // Google Play services was not available for some reason
        } else {
            return false;
        }
    }

//    public int getLocationCount() {
//        return 0;
//    }

    public interface LocationCallBack {
        public void onLocationCallBack(Location location);
    }

    private LocationCallBack locationCallBack;

    public void setLocationCallBack(LocationCallBack locationCallBack) {
        this.locationCallBack = locationCallBack;
    }


    private void reportLastLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            Location lastKnownLocation = mLocationClient.getLastLocation();
            if (lastKnownLocation != null) {
                reportNewLocationReceived(lastKnownLocation);
            }
        }
    }

    private void reportNewLocationReceived(Location location) {
        Intent i = new Intent(ScannerService.MESSAGE_TOPIC);
        i.putExtra(Intent.EXTRA_SUBJECT, LOCATION_SCANNER_EXTRA_SUBJECT);
        i.putExtra(LOCATION_SCANNER_ARG_LOCATION, location);
        i.putExtra("time", System.currentTimeMillis());
        mContext.sendBroadcast(i);
        mLogJsonLocation.dump(location);
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
