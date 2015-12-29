package com.opentrain.app.model;

/**
 * Created by Elina on 15 Dec 2015.
 */
public class StationBasicInfo {
    public String name;
    public String id;

    public StationBasicInfo(String name, String id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
