package il.org.hasadna.opentrain.service;

/**
 * Created by Noam.m on 7/1/2014.
 */

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import il.org.hasadna.opentrain.application.Logger;
import il.org.hasadna.opentrain.application.SharedConstants;

public class GPSScanner extends LocationScanner implements LocationListener {

    public static final String ACTION_GPS_UPDATED = SharedConstants.ACTION_NAMESPACE + ".GPSScanner.GpsUpdated";
    public static final String GPS_SCANNER_ARG_LOCATION = "location";

    public static final String SUBJECT_NEW_STATUS = "new_status";
    public static final String SUBJECT_LOCATION_LOST = "location_lost";
    public static final String SUBJECT_NEW_LOCATION = "new_location";
    public static final String NEW_STATUS_ARG_FIXES = "fixes";
    public static final String NEW_STATUS_ARG_SATS = "sats";

    private static final long GEO_MIN_UPDATE_TIME = 1000;
    private static final float GEO_MIN_UPDATE_DISTANCE = 10;
    private static final int MIN_SAT_USED_IN_FIX = 3;

    private GpsStatus.Listener mGPSListener;

    private int mLocationCount;
    private double mLatitude;
    private double mLongitude;

    public GPSScanner(Context context) {
        super(context);
    }

    public void start() {
        startActiveMode();
    }

    private void startActiveMode() {
        LocationManager lm = getLocationManager();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                GEO_MIN_UPDATE_TIME,
                GEO_MIN_UPDATE_DISTANCE,
                this);

        reportLocationLost();
        mGPSListener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    GpsStatus status = getLocationManager().getGpsStatus(null);
                    Iterable<GpsSatellite> sats = status.getSatellites();

                    int satellites = 0;
                    int fixes = 0;

                    for (GpsSatellite sat : sats) {
                        satellites++;
                        if (sat.usedInFix()) {
                            fixes++;
                        }
                    }
                    reportNewGpsStatus(fixes, satellites);
                    if (fixes < MIN_SAT_USED_IN_FIX) {
                        reportLocationLost();
                    }
                    if (Logger.logFlag) {
                        Logger.location("onGpsStatusChange - satellites: " + satellites + " fixes: " + fixes);
                    }
                } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    reportLocationLost();
                }
            }
        };
        lm.addGpsStatusListener(mGPSListener);
    }

    public void stop() {
        LocationManager lm = getLocationManager();
        lm.removeUpdates(this);
        reportLocationLost();

        if (mGPSListener != null) {
            lm.removeGpsStatusListener(mGPSListener);
            mGPSListener = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) { // TODO: is this even possible??
            reportLocationLost();
            return;
        }

        String provider = location.getProvider();
        if (!provider.toLowerCase().contains("gps")) {
            // only interested in GPS locations
            return;
        }

        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();

        reportNewLocationReceived(location);
        mLocationCount++;
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            reportLocationLost();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if ((status != LocationProvider.AVAILABLE)
                && (LocationManager.GPS_PROVIDER.equals(provider))) {
            reportLocationLost();
        }
    }

    private LocationManager getLocationManager() {
        return (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private void reportLocationLost() {
        boolean flag = true;
        if (flag) {
            if (Logger.logFlag) {
                Logger.location("location lost!");
            }
        } else {
            Intent i = new Intent(ACTION_GPS_UPDATED);
            i.putExtra(Intent.EXTRA_SUBJECT, SUBJECT_LOCATION_LOST);
            i.putExtra(SharedConstants.NAME_TIME, System.currentTimeMillis());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
        }
    }

    private void reportNewGpsStatus(int fixes, int sats) {
        boolean flag = true;
        if (flag) {
            if (Logger.logFlag) {
                Logger.location("NewGpsStatus!" + fixes + "," + sats);
            }
        } else {
            Intent i = new Intent(ACTION_GPS_UPDATED);
            i.putExtra(Intent.EXTRA_SUBJECT, SUBJECT_NEW_STATUS);
            i.putExtra(NEW_STATUS_ARG_FIXES, fixes);
            i.putExtra(NEW_STATUS_ARG_SATS, sats);
            i.putExtra(SharedConstants.NAME_TIME, System.currentTimeMillis());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
        }
    }
}
