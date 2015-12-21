package jp.gaijins.jobs.feed;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tagmanager.Container;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.gaijins.jobs.R;
import jp.gaijins.jobs.common.GaijinJobsAnalyticsHelper;
import jp.gaijins.jobs.common.GaijinJobsApplication;
import jp.gaijins.jobs.common.NavigationDrawerFragment;
import jp.gaijins.jobs.gtm.ContainerHolderSingleton;


public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String JOB_CATEGORY_KEY = "category";
    private static final String CATEGORY_NAME_KEY = "name";
    private static final String URL_LIST_KEY = "url_list";

    private static final int ID_CATEGORY_ALL = 0;
    private static final int ID_CATEGORY_ENGINEERING = 1;
    private static final int ID_CATEGORY_DESIGN = 2;
    private static final int ID_CATEGORY_MARKETING = 3;
    private static final int ID_CATEGORY_BOOKMARKS = 4;


    // Key is the job category name, and the value is a list of urls.
    private Map<String, ArrayList<String>> mCategoryUrlMap = new HashMap<>();

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolBar;
    private TextView mToolBarTitle;
    private Container container;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    private GaijinJobsAnalyticsHelper mAnalyticsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_color_dark));
        }

        // Setup the toolbar and navigation drawer
        setupToolBar();
        setupNavigationDrawer();

        ContainerHolderSingleton.getContainerHolder().refresh();
        container = ContainerHolderSingleton.getContainerHolder().getContainer();

        mAnalyticsHelper = new GaijinJobsAnalyticsHelper();

        fetchJobUrlList();

    }

    private void fetchJobUrlList() {

        String categoriesJsonString = container.getString(JOB_CATEGORY_KEY);
        mCategoryUrlMap.clear();

        // No category information returned from container.
        if (categoriesJsonString.isEmpty()) {
            Log.e(TAG, "News categories could not be retrieved");
            Toast.makeText(this, "Sorry! Could not retrieve job categories", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // GTM doesn't support returning compound objects, so we store the category map as a JSON
            // string which we then parse.
            JSONArray categories = new JSONArray(categoriesJsonString);
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                String categoryName = category.getString(CATEGORY_NAME_KEY);

                JSONArray urls = category.getJSONArray(URL_LIST_KEY);
                ArrayList<String> urlList = new ArrayList<>(urls.length());
                for (int j = 0; j < urls.length(); j++) {
                    urlList.add(urls.getString(j));
                }
                mCategoryUrlMap.put(categoryName, urlList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parsing the JSON string: [" + categoriesJsonString + "] throw an exception.", e);
            return;
        }
    }

    private void setupToolBar() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (mToolBar != null) {
            mToolBarTitle = (TextView) mToolBar.findViewById(R.id.toolbar_title);
        }
        setSupportActionBar(mToolBar);
    }

    private void setupNavigationDrawer() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, JobCategoryFragment.newInstance(position))
                .commit();
    }

    public void onSectionAttached(int number) {
        Tracker analyticsTracker = ((GaijinJobsApplication) getApplication()).getTracker();

        switch (number) {
            case ID_CATEGORY_ALL:
                mTitle = getString(R.string.title_category_all);
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_NAVIGATION_DRAWER,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_JOBS_ALL);
                break;
            case ID_CATEGORY_ENGINEERING:
                mTitle = getString(R.string.title_category_engineering);
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_NAVIGATION_DRAWER,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_JOBS_ENGINEERING);
                break;
            case ID_CATEGORY_DESIGN:
                mTitle = getString(R.string.title_category_design);
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_NAVIGATION_DRAWER,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_JOBS_DESIGN);
                break;
            case ID_CATEGORY_MARKETING:
                mTitle = getString(R.string.title_category_marketing);
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_NAVIGATION_DRAWER,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_JOBS_MARKETING);
                break;
            case ID_CATEGORY_BOOKMARKS:
                mTitle = getString(R.string.title_category_bookmarks);
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_NAVIGATION_DRAWER,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_JOBS_BOOKMARKS);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(null);
        }

        if (mToolBarTitle != null) {
            mToolBarTitle.setText(mTitle);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
        }
        return super.onCreateOptionsMenu(menu);
    }

    public Map<String, ArrayList<String>> getCompleteJobSource() {
        return mCategoryUrlMap;
    }

    @Override
    public void onBackPressed() {
        // workaround for drawer not closing on back press when updated to appCompat v21
        if (mNavigationDrawerFragment.closeNavigationDrawer()) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    // hide ToolBar on Scroll
    public RecyclerView.OnScrollListener getRecyclerViewScrollListener() {
        return new RecyclerView.OnScrollListener() {
            boolean hideToolBar = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (hideToolBar) {
                    getSupportActionBar().hide();
                } else {
                    getSupportActionBar().show();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 20) {
                    hideToolBar = true;

                } else if (dy < -5) {
                    hideToolBar = false;
                }
            }
        };
    }

    public void fireFeedAnalytics(String eventLabel) {
        Tracker analyticsTracker = ((GaijinJobsApplication) getApplication()).getTracker();
        mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_HOME_FEED, eventLabel);
    }
}
