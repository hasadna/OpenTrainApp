package il.org.hasadna.opentrain.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public final class ScannerService extends Service {

    private Scanner mScanner;
    private Reporter mReporter;

    private final IBinder mBinder = new ScannerBinder();
    private boolean mIsBound;

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

    public String lastStationName() {
        return mScanner.lastStationName();
    }

    public boolean isStationIndication(){
        return mScanner.isStationIndication();
    }

    public String lastBSSID() {
        return mScanner.lastBSSID();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReporter = new Reporter(this);
        mScanner = new Scanner(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReporter.shutdown();
        mScanner = null;
        mReporter = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("testing", false)) {
                mScanner.setMockScanners();
            }
        }
        // keep running!
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mScanner.isScanning()) {
            stopSelf();
        }
        mIsBound = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mIsBound = true;
    }
}
