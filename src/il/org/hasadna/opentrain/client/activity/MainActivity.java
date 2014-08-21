package il.org.hasadna.opentrain.client.activity;

import android.app.ActionBar;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.application.preferences.PrefsUpdater;
import il.org.hasadna.opentrain.service.DateTimeUtils;
import il.org.hasadna.opentrain.service.ScannerService;

public final class MainActivity extends FragmentActivity {
    public static final String ACTION_UPDATE_UI = "UPDATE_UI";
    private static final String LOGTAG = MainActivity.class.getName();
    private static final int NOTIFICATION_ID = 1;
    private static final String INTENT_TURN_OFF = "il.org.hasadna.opentrain.turnMeOff";
    private ScannerService mConnectionRemote;
    private ServiceConnection mConnection;
    private ServiceBroadcastReceiver mReceiver;
    private TextView textViewLastOnTrain, textViewLastreport, textViewReportsSent, textViewStationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "onCreate:");
        enableStrictMode();
        setContentView(R.layout.activity_main);
        PrefsUpdater.scheduleUpdate(this);
        textViewLastOnTrain = (TextView) findViewById(R.id.last_train);
        textViewLastreport = (TextView) findViewById(R.id.last_upload_time);
        textViewReportsSent = (TextView) findViewById(R.id.reports_sent);
        textViewStationName = (TextView) findViewById(R.id.station_name);
        ActionBar actionBar = getActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mReceiver = new ServiceBroadcastReceiver();
        mReceiver.register();

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder binder) {
                Log.i(LOGTAG, "ServiceConnection.onServiceConnected:");
                ScannerService.ScannerBinder serviceBinder = (ScannerService.ScannerBinder) binder;
                mConnectionRemote = serviceBinder.getService();
                startScanning();
                updateUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.i(LOGTAG, "ServiceConnection.onServiceDisconnected:");
                mConnectionRemote = null;
            }
        };

        Intent intent = new Intent(this, ScannerService.class);
        boolean testing = getIntent().getBooleanExtra("testing", false);
        intent.putExtra("testing", testing);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Log.d(LOGTAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mConnection = null;
        mConnectionRemote = null;
        mReceiver.unregister();
        mReceiver = null;
        Log.d(LOGTAG, "onStop");
    }

    protected void updateUI() {

        if (mConnectionRemote == null) {
            return;
        }

        Log.d(LOGTAG, "UpdateUI:");
        boolean scanning = mConnectionRemote.isScanning();

        Button scanningBtn = (Button) findViewById(R.id.toggle_scanning);
        TextView status = (TextView) findViewById(R.id.status_text);
        if (scanning) {
            status.setText(R.string.status_on);
            scanningBtn.setBackgroundResource(R.drawable.switch_on);
        } else {
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.switch_off);
        }

        long lastOnTrain = mConnectionRemote.lastOnTrain();
        long lastReport = mConnectionRemote.lastReport();
        int reportsSent = mConnectionRemote.reportsSent();
        int reportsPending = mConnectionRemote.reportsPending();
        String stationName = mConnectionRemote.stationName();

        String lastTrainIndicationTimeString = (lastOnTrain > 0) ? DateTimeUtils
                .formatTimeForLocale(lastOnTrain) : "--";
        String lastUploadTimeString = (lastReport > 0) ? DateTimeUtils
                .formatTimeForLocale(lastReport) : "--";
        String reportsSentString = String.valueOf(reportsSent);
        String reportsPendingString = String.valueOf(reportsPending);
        String stationNameString = stationName != null && stationName.length() > 0 ? stationName : "--";

        textViewLastOnTrain.setText(lastTrainIndicationTimeString);
        textViewLastreport.setText(lastUploadTimeString);
        textViewReportsSent.setText(reportsSentString);
        textViewStationName.setText(stationNameString);
    }

    public void onClick_ToggleScanning(View v) throws RemoteException {
        if (mConnectionRemote == null) {
            return;
        }

        boolean scanning = mConnectionRemote.isScanning();
        Log.d(LOGTAG, "Connection remote return: isScanning() = " + scanning);

        if (scanning) {
            stopScanning();
        } else {
            startScanning();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.privacy_policy:
                Intent privacyIntent = new Intent(this, PrivacyPolicyActivity.class);
                startActivity(privacyIntent);
                return true;
            case R.id.share_dump_files:
                Intent shareIntent = new Intent(this, ShareDumpFilesActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.settings:
                Intent settings;
                if (Build.VERSION.SDK_INT < 11) {
                    settings = new Intent(this, SettingsActivityOlder.class);
                } else {
                    settings = new Intent(this, SettingsActivity.class);
                }
                startActivity(settings);
                return true;
            default:
                return false;
        }
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll().permitDiskReads().permitDiskWrites().penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog().build());
    }

    private void startScanning() {
        mConnectionRemote.startForeground(NOTIFICATION_ID, buildNotification());
        mConnectionRemote.startScanning();
    }

    private void stopScanning() {
        mConnectionRemote.stopForeground(true);
        mConnectionRemote.stopScanning();
    }

    private Notification buildNotification() {
        Context context = getApplicationContext();
        Intent turnOffIntent = new Intent(INTENT_TURN_OFF);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, turnOffIntent, 0);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_status_scanning)
                .setContentTitle(getText(R.string.service_name))
                .setContentText(getText(R.string.service_scanning))
                .setTicker(getText(R.string.service_scanning))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.notification_close), pendingIntent)
                .build();

    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        private boolean mReceiverIsRegistered;

        public void register() {
            if (!mReceiverIsRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_UPDATE_UI);
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(this,
                        intentFilter);
                mReceiverIsRegistered = true;
            }
        }

        public void unregister() {
            if (mReceiverIsRegistered) {
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(this);
                mReceiverIsRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }
}
