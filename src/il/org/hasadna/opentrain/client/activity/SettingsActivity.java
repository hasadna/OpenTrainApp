package il.org.hasadna.opentrain.client.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import il.org.hasadna.opentrain.client.fragment.SettingsFragment;

/**
 * Created by Noam.m on 4/29/2014.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
