package il.org.hasadna.opentrain.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestManager {

    private static final String TAG = RequestManager.class.getSimpleName();

    public static String getPrefrenceFromServer() {

        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://192.241.154.128/client/config");
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            String result = getStringFromInputStream(in);
            return result;
        } catch (Exception e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "error";
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
