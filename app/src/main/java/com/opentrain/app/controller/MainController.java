package com.opentrain.app.controller;

import android.content.Context;

import com.opentrain.app.network.NetowrkManager;

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

    public void init(Context context) {
        NetowrkManager.getInstance().init(context);
        NetowrkManager.getInstance().getStopsFromServer(null);
    }
}