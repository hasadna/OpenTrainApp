package il.org.hasadna.opentrain.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.util.Log;

public class RequestManager {

	public static void getPrefrenceFromServer() {

		try {
			InputStream in = getInputStream("http://www.android.com/");
			String result = getStringFromInputStream(in);
			parseResponse(result);
		} catch (Exception e) {

		}
	}

	private static void parseResponse(String result) throws Exception {
		JSONObject jsonObject = new JSONObject(result);
		int WIFI_MIN_UPDATE_TIME = jsonObject.optInt("WIFI_MIN_UPDATE_TIME");
		int WIFI_MODE_TRAIN_FOUND_PERIOD = jsonObject
				.optInt("WIFI_MODE_TRAIN_FOUND_PERIOD");
		int WIFI_MODE_TRAIN_SCANNIG_PERIOD = jsonObject
				.optInt("WIFI_MODE_TRAIN_SCANNIG_PERIOD");
		int LOCATION_API_UPDATE_INTERVAL = jsonObject
				.optInt("LOCATION_API_UPDATE_INTERVAL");
		int LOCATION_API_FAST_CEILING_INTERVAL = jsonObject
				.optInt("LOCATION_API_FAST_CEILING_INTERVAL");
		int RECORD_BATCH_SIZE = jsonObject.optInt("RECORD_BATCH_SIZE");
		int TRAIN_INDICATION_TTL = jsonObject.optInt("TRAIN_INDICATION_TTL");
		Log.d("params", " " + WIFI_MIN_UPDATE_TIME + " "
				+ WIFI_MODE_TRAIN_FOUND_PERIOD + " "
				+ WIFI_MODE_TRAIN_SCANNIG_PERIOD + " "
				+ LOCATION_API_UPDATE_INTERVAL + " "
				+ LOCATION_API_FAST_CEILING_INTERVAL + " " + RECORD_BATCH_SIZE
				+ " " + TRAIN_INDICATION_TTL + " ");
	}

	private static InputStream getInputStream(String urlString) {
		InputStream in = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream());
		} catch (Exception e) {

		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return in;
	}

	private static String getStringFromInputStream(InputStream inputStream)
			throws Exception {
		BufferedReader r = new BufferedReader(
				new InputStreamReader(inputStream));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line);
		}
		return total.toString();
	}

}
