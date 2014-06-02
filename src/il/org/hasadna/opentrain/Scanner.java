package il.org.hasadna.opentrain;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Scanner {
    private static final String LOGTAG = Scanner.class.getName();

    private final Context mContext;
    private boolean mIsScanning;

    private IScanner mWifiScanner;
    private IScanner mLocationScanner;

    public interface IScanner{
        public void start();
        public void stop();
    }

    public Scanner(Context context) {
        mContext = context;

        mWifiScanner = new WifiScanner(context);
        mLocationScanner=new LocationScanner(context);
        ((WifiScanner)mWifiScanner).setLocationScanner(mLocationScanner);
    }

    public Scanner(Context context,IScanner wifiScanner, IScanner locationScanner) {
        //constructor with custom scanners.for testing.
        mContext = context;
        mWifiScanner =wifiScanner;
        mLocationScanner=locationScanner;
    }

    public void startScanning() {
        if (mIsScanning) {
            return;
        }
        Log.d(LOGTAG, "Scanning started...");

        mWifiScanner.start();

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

    public void stopScanning() {
        if (!mIsScanning) {
            return;
        }

        Log.d(LOGTAG, "Scanning stopped");


        mWifiScanner.stop();
        mLocationScanner.stop();

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
}
