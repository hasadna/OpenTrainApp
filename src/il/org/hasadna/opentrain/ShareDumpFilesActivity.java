package il.org.hasadna.opentrain;

import il.org.hasadna.opentrain.monitoring.JsonDumper;

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
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDumpFilesActivity extends Activity {
	
	 private static final String LOGTAG= ShareDumpFilesActivity.class.getName();
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
		 
		 Context context = getApplicationContext();
		 CharSequence text = "onClick_Share:";
		 int duration = Toast.LENGTH_SHORT;
		
		 Toast toast = Toast.makeText(context, text, duration);
		 toast.show();
		 
		 Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		
		 intent.setType("text/plain");
		 intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"eyal.liebermann@gmail.com","noam2030@gmail.com"});
		 intent.putExtra(Intent.EXTRA_CC,	new String[]{"opentrain-dev@googlegroups.com"});
		 intent.putExtra(Intent.EXTRA_SUBJECT, "OpenTrainApp Json Dump Files");
		 intent.putExtra(Intent.EXTRA_TEXT, "OpenTrainApp Json Dump Files attched");
		 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		
		ArrayList<Uri> uris = new ArrayList<Uri>();
	    
	    for (int i=0;i<20 && i<mFilesList.length;i++)
	    {
	    	if(mFilesList[i].exists()&& mFilesList[i].canRead()){
		        Uri u = Uri.fromFile(mFilesList[i]);
		        uris.add(u);
	    	}
	    }
   	   	
		 intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		 startActivity(intent);
		 return;
	 }

	private File[] getFilesList() {
		File[] logFiles = mLogsDir.listFiles(new FileFilter() {
			
			long currentTimeMillis= System.currentTimeMillis();
			long singleDayPeriodMills= TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)/24;//EyalLiebermann error for debugging purpose

			@Override
			public boolean accept(File pathname) {
				boolean retValue = 	(currentTimeMillis - pathname.lastModified()) < singleDayPeriodMills;
				// TODO Auto-generated method stub
				return retValue;
			}
		});

		Arrays.sort(logFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return (int) (0 - Long.valueOf(f1.getName().compareTo(
								f2.getName())));//name is ordered by creation date
			}
		});
		return logFiles;
	}
}