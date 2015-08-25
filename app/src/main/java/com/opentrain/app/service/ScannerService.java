package com.opentrain.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.testing.MockWifiScanner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by noam on 06/06/15.
 */
public class ScannerService extends Service {

    public WifiScanner wifiScanner;
    private Timer timer;
    private Timer stopScanningTimer;

    private ServiceHandler mServiceHandler;

    private final IBinder mBinder = new LocalBinder();
    private int toastId = 1;

    private boolean isScanning;

    public boolean isScanning() {
        return isScanning;
    }

    public ArrayList<Station> getScanningItems() {
        return wifiScanner != null ? wifiScanner.stationsListItems : null;
    }

    public void clearItems() {
        if (wifiScanner != null) {
            wifiScanner.stationsListItems.clear();
            notifyScanResults();
        }
    }

    public void onResume() {
        cancelSchecdleSelfStopScannig();
    }

    public void onPause() {
        schecdleSelfStopScannig();
    }

    public class LocalBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            if (id == toastId) {
                Toast.makeText(ScannerService.this, ((String) msg.obj), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Thread.NORM_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        setTrainWifiScanner();
    }

    public void setTrainWifiScanner() {
        wifiScanner = new WifiScanner(this);
        wifiScanner.setScanningListener(scannResultListener);
    }

    public void setTestWifiScanner() {
        wifiScanner = new MockWifiScanner(this);
        wifiScanner.setScanningListener(scannResultListener);
    }

    private WifiScanner.ScanningListener scannResultListener = new WifiScanner.ScanningListener() {
        @Override
        public void onSannResult() {
            notifyScanResults();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopScanning();
    }

    public void startScannig() {
        isScanning = true;
        notifyStartScanning();

        if (wifiScanner != null) {
            wifiScanner.register(ScannerService.this);
        }

        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scan();
            }
        }, 0, Settings.SCAN_INTERVAL);
    }

    public void schecdleSelfStopScannig() {
        if (stopScanningTimer == null) {
            stopScanningTimer = new Timer();
        }
        stopScanningTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScanning();
            }
        }, 3 * 60 * 60 * 1000);
    }

    public void cancelSchecdleSelfStopScannig() {
        if (stopScanningTimer != null) {
            stopScanningTimer.cancel();
            stopScanningTimer = null;
        }
    }


    private void scan() {
        notifyScanStart();
        wifiScanner.startScanning();
    }

    public void stopScanning() {
        isScanning = false;

        notifyStopScanning();

        if (wifiScanner != null) {
            wifiScanner.unRegister(ScannerService.this);
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void notifyStartScanning() {
        notifyAction(ServiceBroadcastReceiver.ACTION_START_SCANNIG);
    }

    private void notifyScanStart() {
        notifyAction(ServiceBroadcastReceiver.ACTION_START_SCAN);
    }

    private void notifyScanResults() {
        notifyAction(ServiceBroadcastReceiver.ACTION_SCAN_RESULT);
    }

    private void notifyStopScanning() {
        notifyAction(ServiceBroadcastReceiver.ACTION_STOP_SCANNIG);
    }

    private void toast(String str) {
        Message msg = mServiceHandler.obtainMessage();
        msg.what = toastId;
        msg.obj = str;
        mServiceHandler.sendMessage(msg);
    }

    private void notifyAction(String action) {
        Intent i = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
