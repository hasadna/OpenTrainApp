package il.org.hasadna.opentrain.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import il.org.hasadna.opentrain.client.activity.MainActivity;
import test.service.MockLocationScanner;
import test.service.MockWifiScanner;

public final class ScannerService extends Service {

    public static final String MESSAGE_TOPIC = "il.org.hasadna.opentrain.serviceMessage";
    private static final String LOGTAG = ScannerService.class.getName();

    private Scanner mScanner;
    private Reporter mReporter;

    private final IBinder mBinder = new ScannerBinder();
    private boolean mIsBound;
    private boolean mTesting;

    public final class ScannerBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }


    public boolean isScanning() {
        return mScanner.isScanning();
    }

    public void startScanning() {
        if (mScanner.isScanning()) {
            return;
        }
        mScanner.startScanning();
    }


    public void stopScanning() {
        if (mScanner.isScanning()) {
            mScanner.stopScanning();
            mReporter.flush();
            if (!mIsBound) {
                stopSelf();
            }
            //SyncUtils.TriggerRefresh(false);
        }
    }

    public long lastOnTrain() {
        return mScanner.lastOnTrain();
    }

    public long lastReport() {
        return mScanner.lastReport();
    }

    public int reportsSent() {
        return mScanner.reportsSent();
    }

    public int reportsPending() {
        return mScanner.reportsPending();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "onCreate:");

        mReporter = new Reporter(this);
        mScanner = new Scanner(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGTAG, "onDestroy");

        mScanner.stopScanning();
        mScanner = null;

        mReporter.shutdown();
        mReporter = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mTesting = intent.getBooleanExtra("testing", false);
            if (mTesting) {
                mScanner.setMockScanners(new MockWifiScanner(this), new MockLocationScanner(this));
            }
        }
        // keep running!
        Log.d(LOGTAG, "onStartCommand:");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        Log.d(LOGTAG, "onBind:");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOGTAG, "onUnbind");
        if (!mScanner.isScanning()) {
            stopSelf();
        }
        mIsBound = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mIsBound = true;
        Log.d(LOGTAG, "onRebind");
    }
}
