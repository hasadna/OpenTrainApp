package com.opentrain.app.application;

import android.app.Application;

import com.opentrain.app.controller.MainController;

/**
 * Created by noam on 29/06/15.
 */
public class OpenTrainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MainController.getInstance().init(this);
    }
}