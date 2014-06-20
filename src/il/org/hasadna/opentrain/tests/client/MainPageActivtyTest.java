package il.org.hasadna.opentrain.tests.client;

import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import il.org.hasadna.opentrain.client.activity.MainActivity;

/**
 * Created by noam on 16/06/2014.
 */
public class MainPageActivtyTest extends ActivityUnitTestCase<MainActivity> {

    public MainPageActivtyTest() {
        super(MainActivity.class);
    }

    @SmallTest
    public void testBlah() {
        assertEquals(1, 1);
    }
}
