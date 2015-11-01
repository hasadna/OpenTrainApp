package com.opentrain.app.model;

/**
 * Created by noam on 07/06/15.
 */
public class LogItem {

    public String msg;
    public long unixTimeMs;

    public LogItem(String str) {
        this.msg = str;
        this.unixTimeMs = System.currentTimeMillis();
    }
}
