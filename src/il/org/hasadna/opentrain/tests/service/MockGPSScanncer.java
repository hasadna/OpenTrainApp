package il.org.hasadna.opentrain.tests.service;

import android.content.Context;
import android.location.Location;

import il.org.hasadna.opentrain.service.GPSScanner;

/**
 * Created by android on 01/07/2014.
 */
public class MockGPSScanncer extends GPSScanner {

    private boolean stop;

    public MockGPSScanncer(Context context) {
        super(context);
    }

    public void start() {
        stop = false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 7; i++) {
                    if (!stop) {
                        Location mockLocation = LocationTestUtils.getMockLocation();
                        reportNewLocationReceived(mockLocation);
                        try {
                            Thread.sleep(1000 * 15);
                        } catch (Exception e) {

                        }
                    }
                }
                LocationTestUtils.reportDone(mContext);
            }
        }).start();
    }

    public void stop() {
        stop = true;
    }
}
