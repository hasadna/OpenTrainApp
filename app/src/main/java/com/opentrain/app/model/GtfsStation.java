package com.opentrain.app.model;

/**
 * Created by Elina on 06/10/2015.
 */
public class GtfsStation {
    public String id;
    public String name;
    public long enterUnixTimeMs;
    public Long exitUnixTimeMs;

    public GtfsStation(String id, String name, long enterUnixTimeMs, long exitUnixTimeMs) {
        this.id = id;
        this.name = name;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = exitUnixTimeMs;
    }

    public GtfsStation(String id, long enterUnixTimeMs, long exitUnixTimeMs) {
        this(id, id, enterUnixTimeMs, exitUnixTimeMs);
    }
}
