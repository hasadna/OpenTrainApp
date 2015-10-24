package com.opentrain.app.controller;

import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;

import java.util.Map;

/**
 * Created by Elina_2 on 23 Oct 2015.
 */
public class UpdateBssidMapAction implements Action {
    private BssidMap bssidMap;

    public UpdateBssidMapAction(BssidMap bssidMap) {
        this.bssidMap = bssidMap;
    }

    public void execute() {
        MainModel.getInstance().setBssidMap(bssidMap);
    }
}
