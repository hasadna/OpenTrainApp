package il.org.hasadna.opentrain.application;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import il.org.hasadna.opentrain.R;

/**
 * Created by Noam.m on 4/30/2014.
 */
public class MainApplication extends Application {

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.analytics);
    }

    public void trackException(String description) {
        if (tracker != null) {
            tracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(description)
                    .build());
        }
    }

    public void trackEvent(String category, String description) {
        if (tracker != null) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(description)
                    .build());
        }
    }
}
