package jp.gaijins.jobs.common;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import jp.gaijins.jobs.R;

/**
 * Created by nayak.vishal on 2015/12/15.
 */
public class GaijinJobsApplication extends Application {

    // Create a Hash map in case global or eCommerce trackers need to be supported
    private Tracker mAppTracker;

    public GaijinJobsApplication() {
        super();
    }

    /**
     * デフォルトのTrackerを返す
     *
     * @return
     */
    public Tracker getTracker() {
        return getTracker(null);
    }

    /**
     * GlobalTracker,e-commerceTracker not supported
     *
     * @param trackerId tracker ID to be retrieved
     * @return Tracker
     */
    public synchronized Tracker getTracker(String trackerId) {
        if (mAppTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);
            if (trackerId == null) {
                mAppTracker = analytics.newTracker(R.xml.analytics_tracker);
            } else {
                mAppTracker = analytics.newTracker(trackerId);
            }
        }
        return mAppTracker;
    }
}
