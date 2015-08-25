package com.opentrain.app;

import android.test.InstrumentationTestCase;
import com.opentrain.app.model.Station;
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

    public void test2AddMappingToServer() throws Throwable {

        Station station = new Station();
        station.stationName = "StationNameTest";
        station.bssids.put("b4:c7:99:0b:aa:c1", null);
        station.bssids.put("b4:c7:99:0b:d4:90", null);

        NetowrkManager.getInstance().addMappingToServer(station.getPostParam(), new NetowrkManager.RequestListener() {
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

}
