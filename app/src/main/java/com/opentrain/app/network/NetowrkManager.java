package com.opentrain.app.network;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentrain.app.model.Trip;
import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.WifiScanResultItem;
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
        JsonArrayRequest stringRequest = new JsonArrayRequest(Settings.url_get_map_from_server,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            parseBSSIDSResponse(response);
                            BssidMap bssidMap = getMapFromString(response);
                            UpdateBssidMapAction updateBssidMapAction = new UpdateBssidMapAction(bssidMap);
                            MainController.execute(updateBssidMapAction);
                            if (requestListener != null) {
                                requestListener.onResponse(MainModel.getBssidMapping());
                                requestListener.onResponse(bssidMap);
                            }
                            Logger.logMap(bssidMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestListener != null) {
                    requestListener.onError();
                }
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

    private void parseBSSIDSResponse(JSONArray jsonArray) throws Exception {

        HashMap<String, String> mapBSSIDToName = new HashMap<>();
        HashMap<String, String> mapBSSIDSToStop = new HashMap<>();
        try {
            for (int i = 0, j = jsonArray.length(); i < j; i++) {
                try {
                    JSONObject station = jsonArray.getJSONObject(i);
                    String bssid = station.getString("bssid");
                    String name = new String(station.getString("name").getBytes("ISO-8859-1"), "UTF-8");
                    mapBSSIDToName.put(bssid, name);

                    JSONObject stopJson = station.getJSONObject("stop");
                    String stopId = stopJson.getString("id");
                    mapBSSIDSToStop.put(bssid, stopId);
                } catch (Exception e) {
                    Logger.log(e.toString());
                }
            }
            MainModel.getInstance().setBssidMap(mapBSSIDToName);
            MainModel.getInstance().setBssidToStopMap(mapBSSIDSToStop);
            Logger.logMap(mapBSSIDToName);
            Logger.logMap(mapBSSIDSToStop);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(e.toString());
        }
    private BssidMap getMapFromString(String response) throws Exception {
        JSONObject jsonObject = new JSONObject(response);
        return new BssidMap(jsonObject);
    }

    public void getStopsFromServer(final RequestListener requestListener) {

        Logger.log("get stops from server. server url:" + Settings.url_get_stops_from_server);
        // Request a json array response from the provided URL.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Settings.url_get_stops_from_server,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<String> stations = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject stop = (JSONObject) response.get(i);
                                String name = new String(stop.getString("name").getBytes("ISO-8859-1"), "UTF-8");
                                stations.add(name);

                            }
                            MainModel.getInstance().setStationList(stations);
                            if (requestListener != null) {
                                requestListener.onResponse(response);
                            }
                        } catch (Exception e) {
                            Logger.log(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestListener != null) {
                    requestListener.onError();
                }
                Logger.log("Error while getting stop list from server " + error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(jsonRequest);
    }

    public void getTripsFromServer(final RequestListener requestListener) {

        Logger.log("get trips from server. server url:" + Settings.url_get_trips_from_server);
        // Request a json array response from the provided URL.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Settings.url_get_trips_from_server,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<Trip> trips = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject tripJson = (JSONObject) response.get(i);
                                Trip trip = new Trip();
                                trip.parse(tripJson);
                                trips.add(trip);
                            }
                            Logger.log("Successfully get " + trips.size() + " trips from server");
                        } catch (Exception e) {
                            Logger.log("error while getting trips from server : " + e.toString());
                        }
                        MainModel.getInstance().setTrips(trips);
                        if (requestListener != null) {
                            requestListener.onResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestListener != null) {
                    requestListener.onError();
                }
                Logger.log("Error while getting trips list from server " + error.getMessage());
            }
        });
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        requestQueue.add(jsonRequest);
    }
}
