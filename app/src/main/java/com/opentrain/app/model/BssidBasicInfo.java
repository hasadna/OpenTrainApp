package com.opentrain.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class BssidBasicInfo {
    public String id;
    public String bssid;
    public String name;
    public String stop_id;

    public BssidBasicInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

//    public BssidBasicInfo(Long id, String bssid, String name, String stop_id) {
//        this.id = id;
//        this.bssid = bssid;
//        this.name = name;
//        this.stop_id = stop_id;
//    }

    @Override
    public String toString() {
        return name;
    }

    public Map<String, String> toMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("uid", stop_id);
        result.put("author", name);
        return result;
    }
}