package il.org.hasadna.opentrain.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.ScannerService;

/**
 * Created by Noam.m on 4/29/2014.
 */
@SuppressWarnings("deprecation")
public class SettingsActivityOlder extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updatePreferences(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        } else if (p instanceof ListPreference) {
            p.setSummary(((ListPreference) p).getEntry());
        }
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreferences(p);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferences(findPreference(key));
        if ("prefUsername".equals(key)) {
            //if service running
            String updatedValue = sharedPreferences.getString(key, "");
            Intent i = new Intent(ScannerService.ACTION_PREFS_UPDATED_BY_USER);
            i.putExtra(Intent.EXTRA_SUBJECT, key + ":" + updatedValue);
            sendBroadcast(i);
        }
    }
}
