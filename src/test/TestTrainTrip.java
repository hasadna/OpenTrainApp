package test;

import android.content.Context;
import android.os.Handler;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.concurrent.CountDownLatch;

import il.org.hasadna.opentrain.Reporter;
import il.org.hasadna.opentrain.Scanner;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class TestTrainTrip extends AndroidTestCase {

    private CountDownLatch signal = new CountDownLatch(1);
    private Handler handler = new Handler();
    private Scanner mScanner;
    private Reporter mReporter;

    private MockLocationScanner.OnDoneListener onDoneListener=new MockLocationScanner.OnDoneListener() {
        @Override
        public void onDone() {
            signal.countDown();
        }
    };

    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext().getApplicationContext();
        mReporter = new Reporter(context);
        MockLocationScanner mockLocationScanner = new MockLocationScanner(context);
        mockLocationScanner.setLocationListener(onDoneListener);
        MockWifiScanner mockWifiScanner = new MockWifiScanner(context);
        mScanner = new Scanner(context, mockWifiScanner, mockLocationScanner);
        mockWifiScanner.setLocationScanner(mockLocationScanner);
        mScanner.startScanning();
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
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
