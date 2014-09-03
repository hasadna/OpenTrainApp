package il.org.hasadna.opentrain.service;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private TaskCallBack tripCallBack;

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

    public interface TaskCallBack {
        public void onTaskDone();
    }

    class GetBSSIDContent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
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

    public class Stop {
        public String stopName;
        public String expDeparture;
        public String expArrival;
    }
}
