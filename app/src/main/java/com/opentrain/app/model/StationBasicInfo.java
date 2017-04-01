package com.opentrain.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class StationBasicInfo {
    public Long id;
    public Long stop_id;
    public String stop_name;
    public String stop_short_name;

    public StationBasicInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public StationBasicInfo(Long id, Long stop_id, String stop_name, String stop_short_name) {
        this.id = id;
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.stop_short_name = stop_short_name;
    }

    @Override
    public String toString() {
        return stop_name;
    }
}
