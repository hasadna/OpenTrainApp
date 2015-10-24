package com.opentrain.app.testing;

import android.content.Context;

import com.opentrain.app.controller.Action;
import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.NewWifiScanResultAction;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.model.WifiScanResultItem;
import com.opentrain.app.service.WifiScanner;
import com.opentrain.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noam on 13/07/15.
 */
public class MockWifiScanner extends WifiScanner {

    public interface MockWifiScanListener {
        void onScanDone();
    }

    List<Action> mockActions;
    private int index;
    public static MockWifiScanListener mockWifiScanListener;

    public MockWifiScanner(Context context, List<Action> mockActions) {
        super(context);
        this.mockActions = mockActions;
        initMockList();
    }

    private void initMockList() {
        index = 0;
    }

    public void startScanning() {
        if (index >= mockActions.size()) {
            if (mockWifiScanListener != null) {
                mockWifiScanListener.onScanDone();
            }
            return;
        }
        if (mockActions.size() > index) {
            Action action = mockActions.get(index);
            if (action instanceof NewWifiScanResultAction) {
                reportScanResult((NewWifiScanResultAction)action);
            } else if (action instanceof UpdateBssidMapAction) {
                // TODO: It's kind of hacky to update BssidMap in MockWifiScanner.
                MainController.execute(action);
            } else {
                throw new UnsupportedOperationException("Unknown Action type");
            }
        } else {
            reportScanResult(new WifiScanResult(System.currentTimeMillis()));
        }
        index++;
    }
}
