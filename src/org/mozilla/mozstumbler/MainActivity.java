package org.mozilla.mozstumbler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String LOGTAG = MainActivity.class.getName();
    private static final String LEADERBOARD_URL = "https://location.services.mozilla.com/leaders";

    private ScannerServiceInterface  mConnectionRemote;
    private ServiceConnection        mConnection;
    private ServiceBroadcastReceiver mReceiver;
    private int                      mGpsFixes;

    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        private boolean mReceiverIsRegistered;

        public void register() {
            if (!mReceiverIsRegistered) {
                registerReceiver(this, new IntentFilter(ScannerService.MESSAGE_TOPIC));
                mReceiverIsRegistered = true;
            }
        }

        public void unregister() {
            if (mReceiverIsRegistered) {
                unregisterReceiver(this);
                mReceiverIsRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ScannerService.MESSAGE_TOPIC)) {
                Log.e(LOGTAG, "Received an unknown intent");
                return;
            }

            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

            if (subject.equals("Notification")) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                Log.d(LOGTAG, "Received a notification intent and showing: " + text);
                return;
            } else if (subject.equals("Reporter")) {
                updateUI();
                Log.d(LOGTAG, "Received a reporter intent...");
                return;
            } else if (subject.equals("Scanner")) {
                mGpsFixes = intent.getIntExtra("fixes", 0);
                updateUI();
                Log.d(LOGTAG, "Received a scanner intent...");
                return;
            } else if (intent.getBooleanExtra("TrainIndication", false)) {
                updateUI();
                Log.d(LOGTAG, "Received train indication...");
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableStrictMode();
        setContentView(R.layout.activity_main);

        Updater.checkForUpdates(this);

        Log.d(LOGTAG, "onCreate");
    }

    private void checkGps() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.app_name)
                .setMessage(R.string.gps_alert_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        }
    }

    private boolean isGoogleApiKeyValid() {
        String apiKey = PackageUtils.getMetaDataString(this, "com.google.android.maps.v2.API_KEY");
        if ("FAKE_GOOGLE_API_KEY".equals(apiKey)) {
            Log.w(LOGTAG, "Fake Google API Key found.");
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkGps();

        mReceiver = new ServiceBroadcastReceiver();
        mReceiver.register();

        mConnection = new ServiceConnection() {
            @Override
			public void onServiceConnected(ComponentName className, IBinder binder) {
                mConnectionRemote = ScannerServiceInterface.Stub.asInterface(binder);
                Log.d(LOGTAG, "Service connected");
                updateUI();
            }

            @Override
			public void onServiceDisconnected(ComponentName className) {
                mConnectionRemote = null;
                Log.d(LOGTAG, "Service disconnected", new Exception());
            }
        };

        Intent intent = new Intent(this, ScannerService.class);
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
        // TODO time this to make sure we're not blocking too long on mConnectionRemote
        // if we care, we can bundle this into one call -- or use android to remember
        // the state before the rotation.

        if (mConnectionRemote == null) {
            return;
        }

        Log.d(LOGTAG, "Updating UI");
        boolean scanning = false;
        try {
            scanning = mConnectionRemote.isScanning();
        } catch (RemoteException e) {
            Log.e(LOGTAG, "", e);
        }

        Button scanningBtn = (Button) findViewById(R.id.toggle_scanning);
        TextView status = (TextView) findViewById(R.id.status_text);
        if (scanning) {
            status.setText(R.string.status_on);
            scanningBtn.setBackgroundResource(R.drawable.red_button);
        } else {
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.green_button);
        }

        int locationsScanned = 0;
        int APs = 0;
        long lastUploadTime = 0;
        long reportsSent = 0;
        long lastTrainIndicationTime = 0; // TODO: should have time when we were last on train
        try {
            locationsScanned = mConnectionRemote.getLocationCount();
            APs = mConnectionRemote.getAPCount();
            lastUploadTime = mConnectionRemote.getLastUploadTime();
            reportsSent = mConnectionRemote.getReportsSent();
            lastTrainIndicationTime = mConnectionRemote.getLastTrainIndicationTime();
        } catch (RemoteException e) {
            Log.e(LOGTAG, "", e);
        }

		String lastUploadTimeString = (lastUploadTime > 0) ? DateTimeUtils
				.formatTimeForLocale(lastUploadTime) : "-";
		String lastTrainIndicationTimeString = (lastTrainIndicationTime > 0) ? DateTimeUtils
				.formatTimeForLocale(lastTrainIndicationTime) : "-";

        formatTextView(R.id.gps_satellites, R.string.gps_satellites, mGpsFixes);
        formatTextView(R.id.wifi_access_points, R.string.wifi_access_points, APs);
        formatTextView(R.id.locations_scanned, R.string.locations_scanned, locationsScanned);
        formatTextView(R.id.last_upload_time, R.string.last_upload_time, lastUploadTimeString);
        formatTextView(R.id.reports_sent, R.string.reports_sent, reportsSent);
        formatTextView(R.id.last_train, R.string.last_train, lastTrainIndicationTimeString);
    }

    public void onClick_ToggleScanning(View v) throws RemoteException {
        if (mConnectionRemote == null) {
            return;
        }

        boolean scanning = mConnectionRemote.isScanning();
        Log.d(LOGTAG, "Connection remote return: isScanning() = " + scanning);

        Button scanningBtn = (Button) v;
        TextView status = (TextView) findViewById(R.id.status_text);
        if (scanning) {
            unbindService(mConnection);
            mConnectionRemote.stopScanning();
            status.setText(R.string.status_on);
            scanningBtn.setBackgroundResource(R.drawable.red_button);
        } else {
            Intent intent = new Intent(this, ScannerService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mConnectionRemote.startScanning();
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.green_button);
        }
    }

    public void onClick_ViewLeaderboard(View v) {
        Intent openLeaderboard = new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERBOARD_URL));
        startActivity(openLeaderboard);
    }
    
    /*public void onClick_ViewMap(View v) throws RemoteException {
        // We are starting Wi-Fi scanning because we want the the APs for our
        // geolocation request whose results we want to display on the map.
        if (mConnectionRemote != null) {
            mConnectionRemote.startScanning();
        }

        Log.d(LOGTAG, "onClick_ViewMap");
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }*/

    @TargetApi(9)
    private void enableStrictMode() {
        if (VERSION.SDK_INT < 9) {
            return;
        }

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                                              .detectAll()
                                                              .permitDiskReads()
                                                              .permitDiskWrites()
                                                              .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                                                      .detectAll()
                                                      .penaltyLog().build());
    }

    private void formatTextView(int textViewId, int stringId, Object... args) {
        TextView textView = (TextView) findViewById(textViewId);
        String str = getResources().getString(stringId);
        Log.d("hebrew","str = " + str + " args = " + args);
        str = String.format(str, args);
        textView.setText(str);
    }
}
