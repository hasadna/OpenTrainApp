package il.org.hasadna.opentrain.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import il.org.hasadna.opentrain.R;

public class PrivacyPolicyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);
        TextView textView = (TextView) findViewById(R.id.textView);
        String content=readTextFromResource(R.raw.privacy_policy);
        textView.setText(Html.fromHtml(content));
        Linkify.addLinks(textView, Linkify.ALL);
    }

    private String readTextFromResource(int resourceID) {
        StringBuilder total = new StringBuilder();
        try {
            InputStream inputStream = getResources().openRawResource(resourceID);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }

}

