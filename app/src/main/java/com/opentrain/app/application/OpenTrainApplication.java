package com.opentrain.app.application;

import android.app.Application;

import com.opentrain.app.network.NetowrkManager;

/**
 * Created by noam on 29/06/15.
 */
public class OpenTrainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetowrkManager.getInstance().init(this);
    }
}
