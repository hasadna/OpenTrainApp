package com.opentrain.app.network;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentrain.app.model.BssidBasicInfo;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.StationBasicInfo;
import com.opentrain.app.model.Trip;
import com.opentrain.app.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by noam on 29/06/15.
 */
public class NetworkManager {

    private static RequestQueue requestQueue;
    private static NetworkManager mInstance;

    public interface RequestListener {
        void onResponse(Object response);

        void onError();
    }

    public void init(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private NetworkManager() {

    }

    public static NetworkManager getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkManager();
        }
        return mInstance;
    }

    public void getMapFromServer() {
        Logger.log("Get map from Firebase. Endpoint: " + Settings.firebase_bssids);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stopsRef = database.getReference(Settings.firebase_bssids);

        final HashMap<String, String> stopIdToNameMap = new HashMap<>();
        stopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot bssidDataSnapshot : dataSnapshot.getChildren()) {
                    BssidBasicInfo bssidBasicInfo = bssidDataSnapshot.getValue(BssidBasicInfo.class);
                    stopIdToNameMap.put(bssidBasicInfo.stop_id, bssidBasicInfo.name);
                }
                MainModel.getInstance().setStopIdToStopMap(stopIdToNameMap);
                Logger.logMap(stopIdToNameMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.log(databaseError.toString());
            }
        });
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

    public void getStopsFromServer() {
        Logger.log("Get stops from Firebase. Endpoint: " + Settings.firebase_stops);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stopsRef = database.getReference(Settings.firebase_stops);

        final ArrayList<StationBasicInfo> stations = new ArrayList<>();
        stopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot oneStationDataSnapshot : dataSnapshot.getChildren()) {
                    StationBasicInfo station = oneStationDataSnapshot.getValue(StationBasicInfo.class);
                    stations.add(station);
                }
                MainModel.getInstance().setStationList(stations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.log(databaseError.toString());
            }
        });
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
                                Trip trip = new Trip(tripJson);
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
