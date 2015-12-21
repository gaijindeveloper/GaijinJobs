package jp.gaijins.jobs.common;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by nayak.vishal on 2015/12/15.
 */
public class GaijinJobsAnalyticsHelper {
    private static final String TAG = GaijinJobsAnalyticsHelper.class.getSimpleName();

    // Event Action Name
    public static final String EVENT_ACTION_TAP = "tap";

    // Event Label names for actions
    public static final String EVENT_LABEL_SHARE = "share";
    public static final String EVENT_LABEL_BOOKMARK = "bookmark";

    // Event Label names for actions
    public static final String EVENT_LABEL_JOBS_ALL = "all_jobs_screen";
    public static final String EVENT_LABEL_JOBS_ENGINEERING = "engineering_jobs_screen";
    public static final String EVENT_LABEL_JOBS_DESIGN = "design_jobs_screen";
    public static final String EVENT_LABEL_JOBS_MARKETING = "marketing_jobs_screen";
    public static final String EVENT_LABEL_JOBS_BOOKMARKS = "bookmarked_jobs_screen";

    // Event Category names
    public static final String EVENT_CATEGORY_HOME_FEED = "home_feed";
    public static final String EVENT_CATEGORY_JOB_DETAIL = "job_detail";
    public static final String EVENT_CATEGORY_NAVIGATION_DRAWER = "navigation_drawer";

    // Fire analytics data when a UI component is tapped
    public void sendTapEvent(Tracker tracker, String category, String label) {
        if (category == null) {
            Log.e(TAG, "category is null!");
            return;
        }
        tracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setLabel(label)
                        .setAction(EVENT_ACTION_TAP)
                        .build()
        );
    }
}
