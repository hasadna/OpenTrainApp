package il.org.hasadna.opentrain.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import il.org.hasadna.opentrain.DateTimeUtils;
import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.Reporter;
import il.org.hasadna.opentrain.ScannerService;
import il.org.hasadna.opentrain.ScannerServiceInterface;
import il.org.hasadna.opentrain.R.drawable;
import il.org.hasadna.opentrain.R.id;
import il.org.hasadna.opentrain.R.layout;
import il.org.hasadna.opentrain.R.menu;
import il.org.hasadna.opentrain.R.string;
import il.org.hasadna.opentrain.ScannerServiceInterface.Stub;
import il.org.hasadna.opentrain.preferences.PrefsUpdater;

public final class MainActivity extends FragmentActivity {
    private static final String LOGTAG = MainActivity.class.getName();

    private ScannerServiceInterface mConnectionRemote;
    private ServiceConnection mConnection;
    private ServiceBroadcastReceiver mReceiver;
//    private int mGpsFixes;

    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        private boolean mReceiverIsRegistered;

        public void register() {
            if (!mReceiverIsRegistered) {
                registerReceiver(this, new IntentFilter(
                        ScannerService.MESSAGE_TOPIC));
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
            Log.d(LOGTAG, "onReceive: action=" + action + ", subject=" + subject);


            if (subject.equals("Notification")) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
                Log.d(LOGTAG, "onReceive: Showing as toast notification intent. text="
                        + text);
                return;
            } else if (subject.equals(Reporter.class.getName() + ".upload")) {
                long lastUploadTime = intent.getLongExtra(Reporter.class.getName() + ".lastUploadTime", 0);
                String lastUploadTimeString = (lastUploadTime > 0) ? DateTimeUtils
                        .formatTimeForLocale(lastUploadTime) : "";
                formatTextView(R.id.last_upload_time, R.string.last_upload_time,
                        lastUploadTimeString);

                long reportsSent = intent.getLongExtra(Reporter.class.getName() + ".reportsSent", 0);
                formatTextView(R.id.reports_sent, R.string.reports_sent, String.valueOf(reportsSent));

                long reportsPending =  intent.getLongExtra(Reporter.class.getName()+".reportsPending", 0);
				formatTextView(R.id.reports_pending, R.string.reports_pending, String.valueOf(reportsPending));
				
                Log.d(LOGTAG, "onReceive: Reporter intent. lastUploadTimeString=" + lastUploadTimeString + ", reportsSent=" + reportsSent);

                updateUI();
                return;
            } else if (subject.equals(Reporter.class.getName() + ".trainIndication")) {
                long lastTrainIndicationTime = intent.getLongExtra(Reporter.class.getName() + ".lastTrainIndicationTime", 0);
                String lastTrainIndicationTimeString = (lastTrainIndicationTime > 0) ? DateTimeUtils
                        .formatTimeForLocale(lastTrainIndicationTime) : "";
                formatTextView(R.id.last_train, R.string.last_train,
                        lastTrainIndicationTimeString);
                Log.d(LOGTAG, "onReceive: trainIndication intent. lastTrainIndicationTime=" + lastTrainIndicationTime);

                updateUI();
                return;

            } else if (subject.equals("Scanner")) {
 //               mGpsFixes = intent.getIntExtra("fixes", 0);
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
        Log.i(LOGTAG, "onCreate:");
        enableStrictMode();
        setContentView(R.layout.activity_main);
        PrefsUpdater.scheduleUpdate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOGTAG, "onStart:");

        // Reason for commenting out checkGps() :
        // We are currently working under the assumption that we don't need GPS
        // accuracy
        // and it's better to use Location API and save battery
        // checkGps();

        mReceiver = new ServiceBroadcastReceiver();
        mReceiver.register();

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder binder) {
                Log.i(LOGTAG, "ServiceConnection.onServiceConnected:");

                mConnectionRemote = ScannerServiceInterface.Stub
                        .asInterface(binder);
                try {
                    mConnectionRemote.startScanning();
                } catch (RemoteException e) {
                    Log.w(LOGTAG, "ServiceConnection.onServiceConnected: Failed to start service.", e);
                    e.printStackTrace();
                }
                updateUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.i(LOGTAG, "ServiceConnection.onServiceDisconnected:");
                mConnectionRemote = null;
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
        Log.i(LOGTAG, "onStop:");

        try {
            if (!mConnectionRemote.isScanning()) {
                Intent intent = new Intent(this, ScannerService.class);
                stopService(intent);
            }
            unbindService(mConnection);

            mConnection = null;
            mConnectionRemote = null;
        } catch (Exception ex) {
            // Service was already unbound.
        }
        mReceiver.unregister();
        mReceiver = null;

        Log.d(LOGTAG, "onStop");
    }

    protected void updateUI() {
        // TODO time this to make sure we're not blocking too long on
        // mConnectionRemote
        // if we care, we can bundle this into one call -- or use android to
        // remember
        // the state before the rotation.

        if (mConnectionRemote == null) {
            return;
        }

        Log.d(LOGTAG, "UpdateUI:");
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
            scanningBtn.setBackgroundResource(R.drawable.green_button);
        } else {
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.red_button);
        }
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
            mConnectionRemote.stopScanning();
            status.setText(R.string.status_on);
            scanningBtn.setBackgroundResource(R.drawable.green_button);
        } else {
            mConnectionRemote.startScanning();
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.red_button);
        }
    }

	/*
     * public void onClick_ViewMap(View v) throws RemoteException { // We are
	 * starting Wi-Fi scanning because we want the the APs for our //
	 * geolocation request whose results we want to display on the map. if
	 * (mConnectionRemote != null) { mConnectionRemote.startScanning(); }
	 * 
	 * Log.d(LOGTAG, "onClick_ViewMap"); Intent intent = new Intent(this,
	 * MapActivity.class); startActivity(intent); }
	 */

    @TargetApi(9)
    private void enableStrictMode() {
        if (VERSION.SDK_INT < 9) {
            return;
        }

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll().permitDiskReads().permitDiskWrites().penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog().build());
    }

    private void formatTextView(int textViewId, int stringId, String value) {
        TextView textView = (TextView) findViewById(textViewId);
        String title = getResources().getString(stringId);
        Log.d("formatTextView:", "title = " + title + " value = " + value);
       String content=title+" "+value;
        textView.setText(content);
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

}
