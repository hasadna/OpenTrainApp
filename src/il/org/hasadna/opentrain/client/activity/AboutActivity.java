package il.org.hasadna.opentrain.client.activity;

import il.org.hasadna.opentrain.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.webkit.WebView;
import android.widget.TextView;


public class AboutActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView textView=(TextView)findViewById(R.id.textView_about);
        Linkify.addLinks(textView, Linkify.ALL);
    }

}
