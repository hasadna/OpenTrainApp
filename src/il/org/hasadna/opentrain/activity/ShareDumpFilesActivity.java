package il.org.hasadna.opentrain.activity;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.R.id;
import il.org.hasadna.opentrain.R.layout;
import il.org.hasadna.opentrain.R.string;
import il.org.hasadna.opentrain.monitoring.JsonDumper;
import il.org.hasadna.opentrain.preferences.Prefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDumpFilesActivity extends Activity {
	
	 private static final String LOGTAG= ShareDumpFilesActivity.class.getName();
	private static final int SHARE_DUMP_REQUEST = 7;
	 private File mLogsDir;
	private File[] mFilesList;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG,"OnCreate:");
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.privacy_policy);

        setContentView(R.layout.share_dump_files); 
        
        mLogsDir= JsonDumper.getLogsDir(getApplicationContext());
        
        String logsPath=mLogsDir.getPath();
		Log.d(LOGTAG,"OnCreate: logPath="+logsPath);

        
        TextView textView = (TextView) findViewById(R.id.dump_files_path);
        String title = getResources().getString(R.string.dump_files_path);
        String content=title+" "+logsPath;
        textView.setText(content);    
        
        
        ListView listView =(ListView) findViewById(R.id.dump_files_list);
        
        mFilesList=getFilesList();
        
        String[] values= new String[mFilesList.length];
               
        for(int i=0;i<values.length;i++){
        	values[i]=mFilesList[i].getName();
        }

        
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,
        	              android.R.layout.simple_list_item_1, android.R.id.text1, values);
        
        listView.setAdapter(adapter);
    }
	
	 public void onClick_Share(View v) throws RemoteException {	 
		 Log.i(LOGTAG,"onClick_Share:");
		 Intent intent = PrepareEmailIntent();
		 startActivity(intent);
		 return;
	 }
	 	
	private Intent PrepareEmailIntent() {
		
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		
		 intent.setType("message/rfc822");
		 intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"eyal.liebermann@gmail.com","noam2030@gmail.com"});
		 intent.putExtra(Intent.EXTRA_CC,	new String[]{"opentrain-dev@googlegroups.com"});
		 intent.putExtra(Intent.EXTRA_SUBJECT, PrepareMessageSubject());
		 intent.putExtra(Intent.EXTRA_TEXT, PrepareMessageText());
		 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		
		ArrayList<Uri> uris = getUris();
   	   	
		 intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		return intent;
	}

	

	private String PrepareMessageSubject() {
		Prefs prefs = Prefs.getInstance(getApplicationContext());

		StringBuilder sb = new StringBuilder();

		sb.append(getResources().getString(R.string.dump_email_subject));
		sb.append(" device_id=" + prefs.getDailyID());
		
		return sb.toString();
	}

	private String PrepareMessageText() {
		Prefs prefs = Prefs.getInstance(getApplicationContext());

		StringBuilder sb = new StringBuilder();

		sb.append(getResources().getString(R.string.dump_email_first_line));
		sb.append('\n');
		sb.append('\n');
		sb.append("device_id=" + prefs.getDailyID());
		sb.append('\n');
		sb.append("app_version_code=" + prefs.VERSION_CODE);
		sb.append('\n');
		sb.append("app_version_name=" + prefs.VERSION_NAME);
		sb.append('\n');
		sb.append("config_version=" + prefs.CONFIG_VERSION);
		sb.append('\n');
		sb.append('\n');
		sb.append(getResources().getString(R.string.dump_email_last_line));
		sb.append('\n');
		sb.append('\n');

		return sb.toString();
	}
	
	private ArrayList<Uri> getUris() {
		ArrayList<Uri> uris = new ArrayList<Uri>();
	    for (int i=0;i<20 && i<mFilesList.length;i++)
	    {
	    	if(mFilesList[i].exists()&& mFilesList[i].canRead()){
		        Uri u = Uri.fromFile(mFilesList[i]);
		        uris.add(u);
	    	}
	    }
		return uris;
	}

	private File[] getFilesList() {
		File[] logFiles = mLogsDir.listFiles();
		
		Arrays.sort(logFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return (int) (0 - Long.valueOf(f1.getName().compareTo(
								f2.getName())));//name is ordered by creation date
			}
		});
		return logFiles;
	}
}