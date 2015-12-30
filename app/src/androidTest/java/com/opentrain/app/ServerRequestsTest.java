package com.opentrain.app;

import android.test.InstrumentationTestCase;

import com.opentrain.app.network.NetowrkManager;

import java.util.concurrent.CountDownLatch;

/**
 * Created by noam on 27/07/15.
 */
public class ServerRequestsTest extends InstrumentationTestCase {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public void test1GetMapFromServer() throws Throwable {

        NetowrkManager.getInstance().getMapFromServer(new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {

                assertNotNull(response);
                countDownLatch.countDown();
            }

            @Override
            public void onError() {
                fail();
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
    }

//    public void test2AddMappingToServer() throws Throwable {
//
//        Set<String> bssids = new HashSet<>();
//        bssids.add("b4:c7:99:0b:aa:c1");
//        bssids.add("b4:c7:99:0b:d4:90");
//        String stationName = "StationNameTest";
//
//        Station station = new Station(bssids, System.currentTimeMillis());
//
//        NetowrkManager.getInstance().addMappingToServer(station.getPostParam(stationName), new NetowrkManager.RequestListener() {
//            @Override
//            public void onResponse(Object response) {
//
//                assertNotNull(response);
//                countDownLatch.countDown();
//            }
//
//            @Override
//            public void onError() {
//                fail();
//                countDownLatch.countDown();
//            }
//        });
//
//        countDownLatch.await();
//    }

}
