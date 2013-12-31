package il.org.hasadna.opentrain.preferences;


import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public final class Prefs {
	private static final String LOGTAG = Prefs.class.getName();
	private static final String PREFS_FILE = Prefs.class.getName();
	private static final String REPORTS_PREF = "reports";
	private static final String SEED_CREATED_ON = "seed_created_on";
	private static final String DAILY_SEED = "daily_seed";

	private static final int DATE_CHANGE_DELAY_HOURS = 5; // late trains will
															// count in the
															// previous day

	private int mCurrentVersion;
	private Context mContext;

	public Prefs(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_ACTIVITIES);
			mCurrentVersion = pi.versionCode;
		} catch (PackageManager.NameNotFoundException exception) {
			Log.e(LOGTAG, "getPackageInfo failed", exception);
			mCurrentVersion = 0;
		}
		mContext = context;
	}

	public void setReports(String json) {
		setStringPref(REPORTS_PREF, json);
	}

	public String getReports() {
		return getStringPref(REPORTS_PREF);
	}

	public String getDailyID() {
		// Prepare a string consisting of the year and day. This string will not
		// change at midnight, but rather in the early morning.
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, -DATE_CHANGE_DELAY_HOURS);
		String nowDate = "" + now.get(Calendar.YEAR)
				+ now.get(Calendar.DAY_OF_YEAR);
		String existingSeedDate = getStringPref(SEED_CREATED_ON);

		// Create a new seed if current one is out of date
		if (existingSeedDate == null || !existingSeedDate.equals(nowDate)) {
			Log.d(LOGTAG, "Creating new daily random ID...");
			Random r = new Random();
			String new_id;
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-1");
				byte[] byteArray = new byte[digest.getDigestLength()];
				r.nextBytes(byteArray);
				digest.reset();
				{
					digest.update(byteArray);
					// Digest more stuff here.
				}
				new_id = bytesToHex(digest.digest());
			} catch (Exception ex) {
				Log.w(LOGTAG,
						"Unable to hash device ID, using random long only:"
								+ ex);
				byte[] byteArray = new byte[20];
				r.nextBytes(byteArray);
				new_id = bytesToHex(byteArray);
			}

			SharedPreferences.Editor editor = getPrefs().edit();
			editor.putString(DAILY_SEED, new_id);
			editor.putString(SEED_CREATED_ON, nowDate);
			apply(editor);
			Log.d(LOGTAG, "New daily random ID is: " + new_id);
		}
		return getPrefs().getString(DAILY_SEED, "");
	}

	private String getStringPref(String key) {
		return getPrefs().getString(key, null);
	}

	private void setStringPref(String key, String value) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString(key, value);
		apply(editor);
	}

	@SuppressLint("NewApi")
	private static void apply(SharedPreferences.Editor editor) {
		if (VERSION.SDK_INT >= 9) {
			editor.apply();
		} else if (!editor.commit()) {
			Log.e(LOGTAG, "", new IllegalStateException("commit() failed?!"));
		}
	}

	private SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(PREFS_FILE,
				Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
	}

	public static String bytesToHex(byte[] in) {
		final StringBuilder builder = new StringBuilder();
		for (byte b : in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();

	}
}
