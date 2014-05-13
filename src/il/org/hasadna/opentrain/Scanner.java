package il.org.hasadna.opentrain;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

class Scanner {
    private static final String LOGTAG = Scanner.class.getName();

    private final Context mContext;
    private boolean mIsScanning;

    //private GPSScanner     mGPSScanner;
    //private CellScanner    mCellScanner;
    private WifiScanner mWifiScanner;
    private LocationScanner mLocationScanner;

    Scanner(Context context) {
        mContext = context;

        mWifiScanner = new WifiScanner(context);
        mLocationScanner=new LocationScanner(context);
        mWifiScanner.setLocationScanner(mLocationScanner);
        //mCellScanner = new CellScanner(context);
        // mGPSScanner  = new GPSScanner(context);
    }

    void startScanning() {
        if (mIsScanning) {
            return;
        }
        Log.d(LOGTAG, "Scanning started...");

        mWifiScanner.start();
        // commenting out all CellScanner usage for now:
        //mCellScanner.start();
        //mGPSScanner.start();

        mIsScanning = true;

        // FIXME convey "start" event here?
        // for now all we want is to update the UI anyway
        Intent startIntent = new Intent(ScannerService.MESSAGE_TOPIC);
        startIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanner");
        mContext.sendBroadcast(startIntent);
    }

    void startWifiOnly() {
        mWifiScanner.start();
    }

    void stopScanning() {
        if (!mIsScanning) {
            return;
        }

        Log.d(LOGTAG, "Scanning stopped");


        mWifiScanner.stop();
        mLocationScanner.stop();
        // commenting out all CellScanner usage for now:
        //mCellScanner.stop();
        //mGPSScanner.stop();

        mIsScanning = false;

        // FIXME convey "stop" event here?
        // for now all we want is to update the UI anyway
        Intent stopIntent = new Intent(ScannerService.MESSAGE_TOPIC);
        stopIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanner");
        mContext.sendBroadcast(stopIntent);
    }

    boolean isScanning() {
        return mIsScanning;
    }
//
//    int getAPCount() {
//        return mWifiScanner.getAPCount();
//    }
//
//    int getLocationCount() {
//        //return mGPSScanner.getLocationCount();
//        return mLocationScanner.getLocationCount();
//    }
}
