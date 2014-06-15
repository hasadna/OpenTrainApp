package il.org.hasadna.opentrain.client.activity;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.application.monitoring.JsonDumper;
import il.org.hasadna.opentrain.application.preferences.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDumpFilesActivity extends Activity {

	private static final String LOGTAG = ShareDumpFilesActivity.class.getName();
	private File[] mFilesList;

	public class FilesListAdapter extends ArrayAdapter<File> {

		public FilesListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		public FilesListAdapter(Context context, int resource, List<File> items) {
			super(context, resource, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi;
				vi = LayoutInflater.from(getContext());
				v = vi.inflate(R.layout.file_list_item, null);
			}

			File file = getItem(position);
			if (file != null) {
				TextView tv = (TextView) v.findViewById(R.id.label);
				tv.setBackgroundColor(position);
				if (tv != null) {
					tv.setText(file.getName());
				}
			}
			return v;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "OnCreate:");
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.privacy_policy);

		setContentView(R.layout.share_dump_files);

		File logsDir;

		logsDir = JsonDumper.getLogsDir(getApplicationContext());
		String logsPath = logsDir.getPath();
		TextView textView = (TextView) findViewById(R.id.dump_files_path);
		String title = getResources().getString(R.string.dump_files_path);
		String content = title + " " + logsPath;
		textView.setText(content);
		
		mFilesList = getFilesList(logsDir);
		createListView();
	}

	private void createListView() {
		ListView listView = (ListView) findViewById(R.id.dump_files_list);
		FilesListAdapter adapter = new FilesListAdapter(
				getApplicationContext(), R.layout.file_list_item,
				Arrays.asList(mFilesList));
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {		
		 @Override
		 public void onItemClick(AdapterView<?> adapter, View v, int position,
		 long arg3) {
			 File file=(File) adapter.getItemAtPosition(position); 		
			 Toast.makeText(getApplicationContext(), file.getAbsolutePath(),  Toast.LENGTH_SHORT).show();

			 Intent intent=new Intent(Intent.ACTION_VIEW);
			 intent.setDataAndType(Uri.fromFile(file),"text/plain");
			 try{
				 startActivity(intent);
			 }
			 catch(ActivityNotFoundException e){
				 Log.w(LOGTAG,"createListView.listView.setOnItemClickListener.OnItemClickListener: No Activity found to handle Intent.");		
				 Toast.makeText(getApplicationContext(),R.string.no_activity_found_to_view_file, Toast.LENGTH_LONG).show();
			 }
		 }
		 });
	}

	public void onClick_Share(View v) throws RemoteException {
		Log.i(LOGTAG, "onClick_Share:");
		Intent intent = PrepareEmailIntent();
		startActivity(intent);
		return;
	}

	private Intent PrepareEmailIntent() {

		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {
				"eyal.liebermann@gmail.com", "noam2030@gmail.com" });
		intent.putExtra(Intent.EXTRA_CC,
				new String[] { "opentrain-dev@googlegroups.com" });
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
		for (int i = 0; i < 20 && i < mFilesList.length; i++) {
			if (mFilesList[i].exists() && mFilesList[i].canRead()) {
				Uri u = Uri.fromFile(mFilesList[i]);
				uris.add(u);
			}
		}
		return uris;
	}

	private File[] getFilesList(File logsDir) {
		File[] logFiles = logsDir.listFiles();

		Arrays.sort(logFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return (int) (0 - Long.valueOf(f1.getName().compareTo(
						f2.getName())));// name is ordered by creation date
			}
		});
		return logFiles;
	}
}