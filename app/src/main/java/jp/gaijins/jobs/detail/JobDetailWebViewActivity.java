package jp.gaijins.jobs.detail;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import jp.gaijins.jobs.R;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class JobDetailWebViewActivity extends ActionBarActivity {

    private static final String PARENT_ACTIVITY_METADATA_NAME = "android.support.PARENT_ACTIVITY";
    private Toolbar mToolBar;
    private JobDetailWebViewFragment mJobDetailWebViewFragment;

    private Button mRefreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobs_detail_webview_activity);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_color_dark));
        }

        // Setup the toolbar
        setupToolBar();

        mJobDetailWebViewFragment = (JobDetailWebViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.jobs_detail_webview_fragment);

        mRefreshButton = (Button) findViewById(R.id.RefreshButton);
        if (mRefreshButton != null) {
            mRefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mJobDetailWebViewFragment != null) mJobDetailWebViewFragment.refresh();
                }
            });
        }

    }

    private void setupToolBar() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    public ActionBar getActivityActionBar() {

        return getSupportActionBar();
    }

    @Override
    public void onBackPressed() {
        boolean navigatedBack = false;

        // first try to go back browsing history
        if (mJobDetailWebViewFragment != null) {
            navigatedBack = mJobDetailWebViewFragment.tryBrowserHistoryBack();
        }
        // if not, rely on default implementation
        if (!navigatedBack) {
            super.onBackPressed();
        }
    }

    public boolean hasParentActivityMetadata() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(this.getComponentName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);

            Bundle bundle = ai.metaData;
            if (bundle == null) return false;

            String parentActivityName = bundle.getString(PARENT_ACTIVITY_METADATA_NAME);
            return parentActivityName != null;

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }
}
