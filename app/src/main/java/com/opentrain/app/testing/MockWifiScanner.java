package com.opentrain.app.testing;

import android.content.Context;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.ScanResultItem;
import com.opentrain.app.service.WifiScanner;

import java.util.ArrayList;

/**
 * Created by noam on 13/07/15.
 */
public class MockWifiScanner extends WifiScanner {

    public interface MockWifiScanListener {
        void onScanDone();
    }

    ArrayList<ArrayList<ScanResultItem>> mockResultsList;
    private int index;
    public static MockWifiScanListener mockWifiScanListener;

    public MockWifiScanner(Context context) {
        super(context);
        initMockList();
    }

    private void initMockList() {

        mockResultsList = MainModel.getInstance().getMockResultsList();

        index = 0;

        map = MainModel.getInstance().getMap();
    }

    public void startScanning() {

        if (index >= mockResultsList.size()) {
            if (mockWifiScanListener != null) {
                mockWifiScanListener.onScanDone();
            }
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        reportScanResult(getScanResult());
        index++;
    }

    private ArrayList<ScanResultItem> getScanResult() {
        if (mockResultsList.size() > index) {
            return mockResultsList.get(index);
        }
        return null;
    }
}
