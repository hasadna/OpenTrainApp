package il.org.hasadna.opentrain.service;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import il.org.hasadna.opentrain.R;

/**
 * Created by noam on 20/08/2014.
 */
public class BssidUtils {

    private static HashMap<String, String> bssids = new HashMap<String, String>();

    public static void init(Context context) {

        try {
            Resources res = context.getResources();
            InputStream is = res.openRawResource(R.raw.bssids);

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr, 8192);

            try {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] strs = line.split(" ");
                    String value = "";
                    for (int i = 2, j = strs.length - 1; i < j; i++) {
                        value += strs[i] + " ";
                    }
                    bssids.put(strs[0], value);
                }
            } finally {
                br.close();
            }

        } catch (Exception e) {

        }
    }

    public static String getName(String bssid) {
        String value = bssids.get(bssid);
        return null != value ? value : "";
    }
}
