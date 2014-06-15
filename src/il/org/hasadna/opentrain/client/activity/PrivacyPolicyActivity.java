package il.org.hasadna.opentrain.client.activity;

import il.org.hasadna.opentrain.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class PrivacyPolicyActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);
        WebView webView = (WebView) findViewById(R.id.privacy_policy_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(readTextFromResource(R.raw.privacy_policy_base64),"text/html; charset=utf-8", "base64");
        
    }
    
    private String readTextFromResource(int resourceID)
    {
		StringBuilder total = new StringBuilder();

    	try{
    		InputStream inputStream = getResources().openRawResource(resourceID);
    		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
    		String line;
    		while ((line = r.readLine()) != null) {
	           total.append(line);
    		}
    	}     
       catch (IOException e){
    	   e.printStackTrace();
       }
     
       return total.toString();
     
    }

}

