package il.org.hasadna.opentrain.service;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by android on 29/08/2014.
 */
public class DataManager {

    private static final long BSSID_TIME_STAMP = 1000 * 60 * 60;
    private static DataManager mInstance;
    private HashMap<String, String> bssids = new HashMap<String, String>();
    private long bssidsStamp;
    private boolean bssidTaskFlag;
    private String tripId;
    private boolean tripIdTaskFlag;
    private ArrayList<Stop> tripStops;
    private ArrayList<Stop> stops;
    private TaskCallBack tripCallBack;
    private TaskCallBack stopsCallBack;

    public static DataManager getInstance() {
        if (mInstance == null) {
            synchronized (DataManager.class) {
                if (mInstance == null) {
                    mInstance = new DataManager();
                }
            }
        }
        return mInstance;
    }

    public String getName(String bssid) {
        if (System.currentTimeMillis() - bssidsStamp > BSSID_TIME_STAMP) {
            if (!bssidTaskFlag) {
                bssidTaskFlag = true;
                new GetBSSIDContent().execute();
            }
            return "";
        } else {
            String value = bssids.get(bssid);
            return null != value ? value : "";
        }
    }

    public void getTripInfo(String response) {
        String trip_id = parseUploadResponse(response);
        if (trip_id != null && !trip_id.equals("null") && !trip_id.equals(tripId)) {
            tripId = trip_id;
            if (!tripIdTaskFlag) {
                tripIdTaskFlag = true;
                new GetTripInfoContent().execute(trip_id);
            }
        }
    }

    private String parseUploadResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("cur_gtfs_trip_id");
        } catch (Exception e) {

        }
        return null;
    }

    public ArrayList<Stop> getTripStopList() {
        return tripStops;
    }

    public void registerTripResult(TaskCallBack taskCallBack) {
        this.tripCallBack = taskCallBack;
    }

    public void unRegisterTripResult() {
        this.tripCallBack = null;
    }

    public void unRegisterStationResult() {
        this.stopsCallBack = null;
    }

    public void getStopsList(TaskCallBack taskCallBack) {
        this.stopsCallBack = taskCallBack;
        if (stops == null) {
            new GetStopsTask().execute();
        } else {
            taskCallBack.onTaskDone();
        }
    }

    public ArrayList<Stop> getStopsList() {
        return stops;
    }

    public void sendStopToServer(Stop selected) {
        new SendStopToServer().execute(selected);
    }

    public interface TaskCallBack {
        public void onTaskDone();
    }

    class GetBSSIDContent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLsUtils.URLStopsBssids);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                JSONObject jsonObject = new JSONObject(total.toString());
                Iterator<String> iter = jsonObject.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        JSONObject value = (JSONObject) jsonObject.get(key);
                        String stop_name = value.getString("stop_short_name");
                        bssids.put(key, stop_name);
                    } catch (JSONException e) {
                        // Something went wrong!
                    }
                }
                bssidsStamp = System.currentTimeMillis();
            } catch (Exception e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            bssidTaskFlag = false;
            return null;
        }
    }

    class GetStopsTask extends AsyncTask<String, Void, ArrayList<Stop>> {

        @Override
        protected ArrayList<Stop> doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLsUtils.URLStops);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                JSONArray jsonArray = new JSONArray(total.toString());
                ArrayList<Stop> result = new ArrayList<Stop>();
                for (int i = 0, j = jsonArray.length(); i < j; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Stop stop = new Stop();
                    stop.stopShortName = jsonObject.optString("stop_short_name");
                    stop.stopName = jsonObject.optString("stop_name");
                    stop.gtfsStopId = jsonObject.optString("gtfs_stop_id");
                    JSONArray bssids = jsonObject.optJSONArray("bssids");
                    if (bssids != null) {
                        stop.bssids = new ArrayList<String>();
                        for (int k = 0; k < bssids.length(); k++) {
                            String bssid = bssids.getString(k);
                            stop.bssids.add(bssid);
                        }
                    }
                    result.add(stop);
                }
                return result;
            } catch (Exception e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> result) {
            stops = result;
            if (stopsCallBack != null) {
                stopsCallBack.onTaskDone();
            }
        }
    }

    class GetTripInfoContent extends AsyncTask<String, Void, ArrayList<Stop>> {

        @Override
        protected ArrayList<Stop> doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            ArrayList<Stop> result = new ArrayList<Stop>();
            try {
                String trip_id = strings[0];
                URL url = new URL(String.format(URLsUtils.URLTripInfo, trip_id));
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                JSONObject jsonObject = new JSONObject(total.toString());
                JSONArray stop_times = jsonObject.getJSONArray("stop_times");

                for (int i = 0, j = stop_times.length(); i < j; i++) {
                    JSONObject object = stop_times.getJSONObject(i);
                    Stop stop = new Stop();
                    stop.stopName = object.getJSONObject("stop").getString("stop_name");
                    stop.expDeparture = object.getString("exp_departure");
                    stop.expArrival = object.getString("exp_arrival");
                    stop.expDeparture = DateTimeUtils.formatTimeForLocale(stop.expDeparture);
                    stop.expArrival = DateTimeUtils.formatTimeForLocale(stop.expArrival);

                    result.add(stop);
                }
            } catch (Exception e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> result) {
            tripIdTaskFlag = false;
            tripStops = result;
            if (tripCallBack != null) {
                tripCallBack.onTaskDone();
            }
        }
    }

    class SendStopToServer extends AsyncTask<Stop, Void, String> {

        @Override
        protected String doInBackground(Stop... param) {

            Stop stop = param[0];
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLsUtils.URLAddStop);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);

                JSONObject wrapper = new JSONObject();
                wrapper.put("items", stop.toJsonObject());
                String wrapperData = wrapper.toString();
                byte[] bytes = wrapperData.getBytes();
                urlConnection.setFixedLengthStreamingMode(bytes.length);
                OutputStream out = new BufferedOutputStream(
                        urlConnection.getOutputStream());
                out.write(bytes);
                out.flush();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode >= 200 && responseCode <= 299) {

                }

                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder(in.available());
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                r.close();
            } catch (Exception ex) {
                Log.d("", ex.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "";
        }
    }

    public class Stop {
        public String stopName;
        public String stopShortName;
        public String gtfsStopId;
        public String lat;
        public String lon;
        public ArrayList<String> bssids;
        public String expDeparture;
        public String expArrival;
        public boolean isChecked;

        public JSONObject toJsonObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("gtfs_stop_id", gtfsStopId);
                jsonObject.put("stop_name", stopName);
                if (bssids != null) {
                    JSONArray bssidsJsonArray = new JSONArray();
                    for (String bssid : bssids) {
                        bssidsJsonArray.put(bssid);
                    }
                    jsonObject.put("bssids", bssidsJsonArray);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }
}
