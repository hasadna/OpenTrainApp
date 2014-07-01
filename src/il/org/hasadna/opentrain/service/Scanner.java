package il.org.hasadna.opentrain.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import il.org.hasadna.opentrain.client.activity.MainActivity;
import il.org.hasadna.opentrain.tests.service.MockGPSScanncer;
import il.org.hasadna.opentrain.tests.service.MockLocationScanner;
import il.org.hasadna.opentrain.tests.service.MockWifiScanner;

public class Scanner {
    private static final String LOGTAG = Scanner.class.getName();

    private final Context mContext;
    private boolean mIsScanning;

    private WifiScanner mWifiScanner;
    private LocationScanner mLocationScanner;

    public Scanner(Context context) {
        mContext = context;
        mWifiScanner = new WifiScanner(context);
        mLocationScanner = new LocationScanner(context);
        mWifiScanner.setLocationScanner(mLocationScanner);
    }

    public void setMockScanners() {
        mWifiScanner = new MockWifiScanner(mContext);
        mLocationScanner = new MockGPSScanncer(mContext);
        mWifiScanner.setLocationScanner(mLocationScanner);
    }

    public void startScanning() {
        if (mIsScanning) {
            return;
        }
        Log.d(LOGTAG, "Scanning started...");

        mWifiScanner.start();

        mIsScanning = true;

        Intent startIntent = new Intent(MainActivity.ACTION_UPDATE_UI);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(startIntent);
    }

    public void stopScanning() {
        if (!mIsScanning) {
            return;
        }

        Log.d(LOGTAG, "Scanning stopped");

        mWifiScanner.stop();
        mLocationScanner.stop();

        mIsScanning = false;

        Intent startIntent = new Intent(MainActivity.ACTION_UPDATE_UI);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(startIntent);
    }

    boolean isScanning() {
        return mIsScanning;
    }

    public long lastOnTrain() {
        return mWifiScanner.getmLastTrainIndicationTime();
    }

    public long lastReport() {
        return Submitter.getInstance(mContext).lastReport();
    }

    public int reportsSent() {
        return Submitter.getInstance(mContext).reportSent();
    }

    public int reportsPending() {
        return Submitter.getInstance(mContext).reportsPending();
    }
}
