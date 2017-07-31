package com.opentrain.app.controller;

import android.content.Context;

import com.opentrain.app.model.MainModel;
import com.opentrain.app.network.NetworkManager;
import com.opentrain.app.utils.Logger;

/**
 * Created by noam on 11/09/15.
 */
public class MainController {

    private static MainController mInstance;

    public static MainController getInstance() {
        if (mInstance == null) {
            mInstance = new MainController();
        }
        return mInstance;
    }

    public static void execute(Action action) {
        if (action instanceof NewWifiScanResultAction) {
            Logger.log("Executing NewWifiScanResultAction.");
            ((NewWifiScanResultAction)action).execute();
        } else if (action instanceof UpdateBssidMapAction) {
            Logger.log("Executing UpdateBssidMapAction.");
            ((UpdateBssidMapAction)action).execute();
        } else {
            throw new UnsupportedOperationException("Unknown Action type");
        }
        MainModel.getInstance().addToHistory(action);
//        // Save json to log:
//        try {
//            Logger.log(MainModel.getInstance().historyToJson().toString(2));
//        } catch (Exception e) {}
    }

    public void init(Context context) {
        NetworkManager.getInstance().init(context);
        NetworkManager.getInstance().getStopsFromServer();
        NetworkManager.getInstance().getMapFromServer();
        NetworkManager.getInstance().getTripsFromServer(null);
    }

}