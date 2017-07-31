package com.opentrain.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class BssidBasicInfo {
    public String id;
    public String bssid;
    public String name;
    public String stop_id;

    public BssidBasicInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    @Override
    public String toString() {
        return name;
    }

}