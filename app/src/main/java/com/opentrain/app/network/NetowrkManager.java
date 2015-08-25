package com.opentrain.app.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentrain.app.model.ScanResultItem;
import com.opentrain.app.utils.Logger;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.MainModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by noam on 29/06/15.
 */
public class NetowrkManager {

    private static RequestQueue requestQueue;
    private static NetowrkManager mInstance;

    public interface RequestListener {
        void onResponse(Object response);

        void onError();
    }

    public void init(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private NetowrkManager() {

    }

    public static NetowrkManager getInstance() {
        if (mInstance == null) {
            mInstance = new NetowrkManager();
        }
        return mInstance;
    }

    public void getMapFromServer(final RequestListener requestListener) {

        Logger.log("get map from server. server url:" + Settings.url_get_map_from_server);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Settings.url_get_map_from_server,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            HashMap<String, String> mapFromString = getMapFromString(response);
                            MainModel.getInstance().updateMap(mapFromString);
                            requestListener.onResponse(mapFromString);
                            Logger.logMap(mapFromString);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onError();
                Logger.log("Error while getting map from server " + error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    public void addMappingToServer(JSONObject jsonObject, final RequestListener requestListener) {

        Logger.log("add map to server. server url: " + Settings.url_add_map_to_server + " ,post params:" + jsonObject.toString());
        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, Settings.url_add_map_to_server, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            requestListener.onResponse(response);
                            Logger.log(response.toString());
                        } else {
                            Logger.log("add map to server response is null");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onError();
                Logger.log("Error while adding map from to server " + error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private HashMap<String, String> getMapFromString(String response) throws Exception {

        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("networks");
            for (int i = 0, j = jsonArray.length(); i < j; i++) {
                try {
                    JSONObject station = jsonArray.getJSONObject(i);
                    map.put(station.getString("bssid"), station.getString("name"));
                } catch (Exception e) {
                    Logger.log(e.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(e.toString());
        }
        return map;
    }

    public void getTestTripFromServer(final RequestListener requestListener) {
        Logger.log("get test from server. server url:" + Settings.url_get_test_from_server);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Settings.url_get_test_from_server,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            ArrayList<ArrayList<ScanResultItem>> list = getTestFromString(response);
                            MainModel.getInstance().setMockResultsList(list);
                            requestListener.onResponse(list);
                            Logger.logList(list);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onError();
                Logger.log("Error while getting test from server");
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private ArrayList<ArrayList<ScanResultItem>> getTestFromString(String response) throws Exception {

        ArrayList<ArrayList<ScanResultItem>> list = new ArrayList<>();

        InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(stream, "UTF-8"));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] strs = line.split(" ");

                if (strs.length > 0) {
                    String key = strs[0];

                    String value = "";
                    if (strs.length > 1) {
                        for (int i = 1; i < strs.length; i++) {
                            value += strs[i];
                            if (i < strs.length - 1) {
                                value += " ";
                            }
                        }
                    }
                    if ("S-ISRAEL-RAILWAYS".equals(value) || "ISRAEL-RAILWAYS".equals(value)) {
                        ArrayList<ScanResultItem> scanResultList = new ArrayList<>();
                        ScanResultItem scanResultItem = new ScanResultItem();
                        scanResultItem.SSID = value;
                        scanResultItem.BSSID = key;
                        scanResultList.add(scanResultItem);
                        list.add(scanResultList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.log(e.toString());
        }

        return list;
    }
}
