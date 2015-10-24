package com.opentrain.test;

import com.opentrain.app.model.WifiScanResult;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Elina on 24 Oct 2015.
 * This test tests the WifiScanResultTest class.
 */
public class WifiScanResultTest {
    @Before
    public void setUp() {
    }

    @Test
    public void testJsonSaveAndLoad() {
        WifiScanResult wifiScanResult1 = new WifiScanResult(System.currentTimeMillis(), "1", "S-ISRAEL-RAILWAYS");
        WifiScanResult wifiScanResult2 = new WifiScanResult(
                System.currentTimeMillis(), "2", "S-ISRAEL-RAILWAYS", "3", "S-ISRAEL-RAILWAYS");
        JSONObject json = wifiScanResult1.toJson();
        String jsonString = json.toString();

        // TODO: Change "" to expected json.
        // TODO: Fix this - jsonString is always null because of missing library implementation in unit test: http://stackoverflow.com/questions/3951274/how-to-unit-test-json-parsing
        assertEquals("", jsonString);
    }
}
