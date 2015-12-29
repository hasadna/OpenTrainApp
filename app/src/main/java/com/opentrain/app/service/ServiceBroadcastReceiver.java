package com.opentrain.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.opentrain.app.view.MainActivity;

/**
 * Created by noam on 06/06/15.
 */
public class ServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_START_SCANNIG = "Action start scanning";
    public static final String ACTION_STOP_SCANNIG = "Action stop scanning";
    public static final String ACTION_START_SCAN = "Action start scan";
    public static final String ACTION_SCAN_RESULT = "Action scan result";

    private final MainActivity mContext;
    private boolean mReceiverIsRegistered;


    public ServiceBroadcastReceiver(Context context) {
        this.mContext = ((MainActivity) context);
    }

    public void register() {
        if (!mReceiverIsRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_START_SCANNIG);
            intentFilter.addAction(ACTION_STOP_SCANNIG);
            intentFilter.addAction(ACTION_START_SCAN);
            intentFilter.addAction(ACTION_SCAN_RESULT);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(this,
                    intentFilter);
            mReceiverIsRegistered = true;
        }
    }

    public void unregister() {
        if (mReceiverIsRegistered) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            mReceiverIsRegistered = false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (ACTION_START_SCANNIG.equals(action)) {
            mContext.onStartScanning();
        } else if (ACTION_STOP_SCANNIG.equals(action)) {
            mContext.onStopScanning();
        } else if (ACTION_START_SCAN.equals(action)) {
            mContext.onStartScan();
        } else if (ACTION_SCAN_RESULT.equals(action)) {
            mContext.onStopScan();
            mContext.onScanResult();
        }
    }
}
