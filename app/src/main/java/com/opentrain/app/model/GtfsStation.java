package com.opentrain.app.model;

/**
 * Created by Elina on 06/10/2015.
 */
public class GtfsStation {
    public long enterUnixTimeMs;
    public Long exitUnixTimeMs;
    public String name;
    public String id;

    public GtfsStation(String name, String id, long enterUnixTimeMs, long exitUnixTimeMs) {
        this.name = name;
        this.id = id;
        this.enterUnixTimeMs = enterUnixTimeMs;
        this.exitUnixTimeMs = exitUnixTimeMs;
    }

    public GtfsStation(String id, long enterUnixTimeMs, long exitUnixTimeMs) {
        this(id, id, enterUnixTimeMs, exitUnixTimeMs);
    }
}
