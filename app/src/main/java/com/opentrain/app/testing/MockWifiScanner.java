package com.opentrain.app.testing;

import android.content.Context;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.service.WifiScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noam on 13/07/15.
 */
public class MockWifiScanner extends WifiScanner {

    public interface MockWifiScanListener {
        void onScanDone();
    }

    List<WifiScanResult> mockResultsList;
    private int index;
    public static MockWifiScanListener mockWifiScanListener;

    public MockWifiScanner(Context context, List<WifiScanResult> mockResultsList) {
        super(context);
        this.mockResultsList = mockResultsList;
        initMockList();
    }

    private void initMockList() {
        index = 0;
    }

    public void startScanning() {
        if (index >= mockResultsList.size()) {
            if (mockWifiScanListener != null) {
                mockWifiScanListener.onScanDone();
            }
            return;
        }

        try {
            Thread.sleep(300);
        } catch (Exception e) {

        }
        reportScanResult(getScanResult());
        index++;
    }

    private WifiScanResult getScanResult() {
        if (mockResultsList.size() > index) {
            return mockResultsList.get(index);
        }
        return new WifiScanResult(System.currentTimeMillis());
    }
}
