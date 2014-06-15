package test.service;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.test.suitebuilder.annotation.SmallTest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import il.org.hasadna.opentrain.client.activity.MainActivity;
import il.org.hasadna.opentrain.application.preferences.Prefs;

/**
 * Created by Noam.m on 6/12/2014.
 */
public class TestTrainTrip extends android.test.ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity activity;
    private CountDownLatch signal = new CountDownLatch(1);
    private String userName;
    private static final String userTestName = "test62";

    private BroadcastReceiver mockLocationReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MockLocationScanner.MOCK_LOCATION_SCANNER_EXTRA_SUBJECT)) {
                try {
                    activity.onClick_ToggleScanning(null);
                } catch (RemoteException e) {

                }
                checkServerResponseForTrip();
            }
        }
    };

    public TestTrainTrip() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        Intent intent = new Intent();
        intent.putExtra("testing", true);
        setActivityIntent(intent);
        activity = getActivity();
        LocalBroadcastManager.getInstance(activity).registerReceiver(mockLocationReciver, new IntentFilter(MockLocationScanner.MOCK_LOCATION_SCANNER_EXTRA_SUBJECT));
        userName = getUserName();
        setTesterName(userTestName);
    }

    @Override
    protected void tearDown() throws Exception {
        setTesterName(userName);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mockLocationReciver);
        super.tearDown();
    }


    @SmallTest
    public void testTrainTrip1() {
        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void checkServerResponseForTrip() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean status = checkServerTrip();
                assertEquals(true, status);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert(status);
                    }
                });

            }
        }).start();
    }

    private void setTesterName(String testName) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("prefUsername", testName);
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private String getUserName() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getString("prefUsername", "");
    }

    private boolean checkServerTrip() {
        try {
            String id = Prefs.getInstance(activity).getDailyID();
            String url = "http://opentrain.hasadna.org.il/api/1/devices/" + id + "/reports/?full=1";
            URL serverUrl = new URL(url);
            InputStream inputStream = serverUrl.openStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
            StringBuilder responseString = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                responseString.append(line);
            }
            JSONObject jsonObject = new JSONObject(responseString.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("objects");
            int size = jsonArray.length();
            for (int i = 0; i < size; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object != null) {
                    return true;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    private void alert(boolean success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Testing").setMessage("test " + (success ? "success!!" : "fail..")).setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                signal.countDown();
            }
        });
        dialog.show();
    }

}
