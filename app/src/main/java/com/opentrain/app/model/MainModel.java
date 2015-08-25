package com.opentrain.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noam on 06/07/15.
 */
public class MainModel {

    private static MainModel mInstance;

    public static MainModel getInstance() {
        if (mInstance == null) {
            mInstance = new MainModel();
        }
        return mInstance;
    }

    private HashMap<String, String> map = new HashMap<>();
    private ArrayList<ArrayList<ScanResultItem>> mockResultsList = new ArrayList<>();

    public void updateMap(HashMap<String, String> results) {
        for (Map.Entry<String, String> serverEntry : results.entrySet()) {
            map.put(serverEntry.getKey(), serverEntry.getValue());
        }
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public ArrayList<ArrayList<ScanResultItem>> getMockResultsList() {
        return mockResultsList;
    }

    public void setMockResultsList(ArrayList<ArrayList<ScanResultItem>> mockResultsList) {
        this.mockResultsList = mockResultsList;
    }
}
