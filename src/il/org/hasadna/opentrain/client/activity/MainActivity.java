package il.org.hasadna.opentrain.client.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.application.preferences.PrefsUpdater;
import il.org.hasadna.opentrain.client.fragment.MainFragment;
import il.org.hasadna.opentrain.client.fragment.StationsFragment;
import il.org.hasadna.opentrain.client.fragment.TripFragment;
import il.org.hasadna.opentrain.service.ScannerService;

public final class MainActivity extends FragmentActivity {
    public static final String ACTION_UPDATE_UI = "UPDATE_UI";
    private static final String LOGTAG = MainActivity.class.getName();
    private static final int NOTIFICATION_ID = 1;
    private static final String INTENT_TURN_OFF = "il.org.hasadna.opentrain.turnMeOff";
    public boolean isRTL;
    private ScannerService mConnectionRemote;
    private ServiceConnection mConnection;
    private ServiceBroadcastReceiver mReceiver;
    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableStrictMode();
        setContentView(R.layout.activity_main);
        isRTL = getResources().getBoolean(R.bool.rtl);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        ArrayList<Fragment> fragementsList = new ArrayList<Fragment>();
        fragementsList.add(new MainFragment());
        fragementsList.add(new TripFragment());
        mPagerAdapter.setFragementsList(fragementsList);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        if (Build.VERSION.SDK_INT >= 11) {
            setupTab();
        }

        PrefsUpdater.scheduleUpdate(this);
    }

    private int getItemIndex(int position) {
        if (isRTL) {
            return 2 - 1 - position;
        } else {
            return position;
        }
    }

    private void setupTab() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    mViewPager.setCurrentItem(getItemIndex(tab.getPosition()));
                }

                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // hide the given tab
                }

                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // probably ignore this event
                }
            };
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_main_title).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_trip_title).setTabListener(tabListener));

            mViewPager.setOnPageChangeListener(
                    new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            getActionBar().setSelectedNavigationItem(getItemIndex(position));
                        }
                    });
        }

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
                mPagerAdapter.getMainFragment().setScannerService(mConnectionRemote);
                startScanning();
                mPagerAdapter.updateUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.i(LOGTAG, "ServiceConnection.onServiceDisconnected:");
                mConnectionRemote = null;
                mPagerAdapter.getMainFragment().setScannerService(null);
            }
        };

        Intent intent = new Intent(this, ScannerService.class);
        boolean testing = getIntent().getBooleanExtra("testing", false);
        intent.putExtra("testing", testing);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mConnection = null;
        mConnectionRemote = null;
        mReceiver.unregister();
        mReceiver = null;
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
            case R.id.about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
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

    private class PagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragementsList;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MainFragment getMainFragment() {
            return (MainFragment) getItem(getItemIndex(0));
        }

        private void updateUI() {
            ((MainFragment) getItem(getItemIndex(0))).updateUI();
            ((TripFragment) getItem(getItemIndex(1))).updateUI();
        }

        public void setFragementsList(ArrayList<Fragment> fragementsList) {
            this.fragementsList = fragementsList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragementsList.get(getItemIndex(position));
        }

        @Override
        public int getCount() {
            return fragementsList != null ? fragementsList.size() : 0;
        }
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
            mPagerAdapter.updateUI();
        }
    }

    private void showDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        android.support.v4.app.FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = StationsFragment.newInstance();
        newFragment.show(ft, "dialog");
    }
}
