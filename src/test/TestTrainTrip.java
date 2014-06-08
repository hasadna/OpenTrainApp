package test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import il.org.hasadna.opentrain.Reporter;
import il.org.hasadna.opentrain.Scanner;
import il.org.hasadna.opentrain.application.MainApplication;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class TestTrainTrip extends ApplicationTestCase<MainApplication> {

    private CountDownLatch signal = new CountDownLatch(1);
    private Scanner mScanner;
    private Reporter mReporter;
    private String testerName;

    private MockLocationScanner.OnDoneListener onDoneListener = new MockLocationScanner.OnDoneListener() {
        @Override
        public void onDone() {
            getServerTrip();
            signal.countDown();
        }
    };

    public TestTrainTrip() {
        super(MainApplication.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext().getApplicationContext();
        setTesterName();
        mReporter = new Reporter(context);
        MockLocationScanner mockLocationScanner = new MockLocationScanner(context);
        mockLocationScanner.setLocationListener(onDoneListener);
        MockWifiScanner mockWifiScanner = new MockWifiScanner(context);
        mScanner = new Scanner(context, mockWifiScanner, mockLocationScanner);
        mockWifiScanner.setLocationScanner(mockLocationScanner);
    }

    @Override
    protected void tearDown() throws Exception {
        mScanner.stopScanning();
        mReporter.shutdown();
        super.tearDown();
    }

    @SmallTest
    public void testTrainTrip1() {
        try {
            mScanner.startScanning();
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setTesterName() {
        testerName = "test_10";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("prefUsername", testerName);
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private void getServerTrip() {
        try {
            String url = "http://opentrain.hasadna.org.il/api/1/devices/" + testerName + "/reports/";
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

                }
            }
        } catch (Exception e) {

        }
    }


}
