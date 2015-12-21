package jp.gaijins.jobs.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;

import java.util.List;

import jp.gaijins.jobs.R;
import jp.gaijins.jobs.common.GaijinJobsAnalyticsHelper;
import jp.gaijins.jobs.common.GaijinJobsApplication;
import jp.gaijins.jobs.common.ObservableScrollViewCallbacks;
import jp.gaijins.jobs.common.ObservableWebView;
import jp.gaijins.jobs.common.ScrollState;
import jp.gaijins.jobs.common.StatusViewHelper;
import jp.gaijins.jobs.entity.JobEntity;
import jp.gaijins.jobs.prefs.CachePreferenceManager;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class JobDetailWebViewFragment extends Fragment implements ObservableScrollViewCallbacks {
    private static final String TAG = JobDetailWebViewFragment.class.getSimpleName();


    private static final String EXTRA_JOB_ENTITY = "job_entity";
    public static final String PREFS_NAME_BOOKMARKS = "jp.gaijins.jobs.PREFERENCE_BOOKMARKS";
    public static final String KEY_BOOKMARKS = "job_bookmarks";
    private static final int GONE_COVER_THRESHOLD = 35;

    private ObservableWebView mWebView;
    private JobDetailWebViewClient mWebViewClient;
    private ProgressBar mProgressBar;
    private ShareActionProvider mShareActionProvider;
    private JobEntity mJobEntity;
    private CachePreferenceManager mCachePreferenceManager;
    private GaijinJobsAnalyticsHelper mAnalyticsHelper;

    // Load error status view
    private StatusViewHelper mStatusViewHelper;
    private StatusViewHelper.StatusViewParams mErrorStatusViewParams;
    private View mWebViewStatusView;
    private boolean mHasErrorOnWebView = false;
    private boolean mGoneCover;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.jobs_webview, container, false);
        v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mWebView = (ObservableWebView) v.findViewById(R.id.webview);
        if (mWebView != null) mWebView.setScrollViewCallbacks(this);

        configureSettings();
        configureClients();
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCachePreferenceManager = new CachePreferenceManager();
        mStatusViewHelper = new StatusViewHelper();
        mAnalyticsHelper = new GaijinJobsAnalyticsHelper();

        loadViews();
        loadUrl();

        setHasOptionsMenu(true);
    }

    private void loadViews() {
        mWebViewStatusView = getActivity().findViewById(R.id.Status);
        mErrorStatusViewParams = new StatusViewHelper.StatusViewParams();
        mErrorStatusViewParams.statusText = getString(R.string.status_text);
        mErrorStatusViewParams.statusTextVisible = true;
        mErrorStatusViewParams.messageText = getString(R.string.status_retry_text);
        mErrorStatusViewParams.messageTextVisible = true;
        mErrorStatusViewParams.nextActionText = getString(R.string.status_button_text);
        mErrorStatusViewParams.nextActionButtonVisible = true;

        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.activity_bar);
    }

    private void loadUrl() {
        mStatusViewHelper.gone(mWebViewStatusView);
        parseIntent(getActivity().getIntent());
    }

    private void parseIntent(Intent intent) {
        if (intent == null) return;

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            mJobEntity = bundle.getParcelable(EXTRA_JOB_ENTITY);
            if (mJobEntity == null) return;

            mWebView.loadUrl(mJobEntity.getJobUrl());
        }
    }

    private void configureSettings() {

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setGeolocationEnabled(true);

        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptEnabled(true);
        settings.setNeedInitialFocus(false);
        settings.setSupportMultipleWindows(false);
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 4);

        if (mWebView != null) {
            mWebView.setScrollbarFadingEnabled(true);
            mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    protected void configureClients() {
        mWebViewClient = new JobDetailWebViewClient();
        if (mWebView != null) {
            mWebView.setWebViewClient(mWebViewClient);
            mWebView.setWebChromeClient(new JobDetailWebChromeClient());
        }
    }

    private class JobDetailWebViewClient extends WebViewClient {

        private String mLastLoadUrl;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            if (mLastLoadUrl != null && !mLastLoadUrl.equals(failingUrl))
                return;

            mHasErrorOnWebView = true;
            mStatusViewHelper.visible(mWebViewStatusView, mErrorStatusViewParams);

            Log.e(TAG, "onReceivedError: " + errorCode + " - " + description);
            return;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.equals(mLastLoadUrl))
                return;

            if (!mHasErrorOnWebView) {
                mStatusViewHelper.gone(mWebViewStatusView);
            }

            mLastLoadUrl = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            View fragmentView = getView();
            if (fragmentView != null) {
                View webviewCover = fragmentView.findViewById(R.id.webviewCover);
                if (mHasErrorOnWebView) {
                    if (webviewCover != null) webviewCover.setVisibility(View.VISIBLE);
                    mHasErrorOnWebView = false;
                } else {
                    if (webviewCover != null) webviewCover.setVisibility(View.GONE);
                }
            }
        }
    }

    private class JobDetailWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (mProgressBar != null) {
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }

                if (!mGoneCover && newProgress >= GONE_COVER_THRESHOLD) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        View webviewCover = activity.findViewById(R.id.webviewCover);
                        if (webviewCover != null) {
                            webviewCover.setVisibility(View.GONE);
                        }
                    }

                    mGoneCover = true;
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.jobs_detail_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem searchMenuItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(searchMenuItem);
        mShareActionProvider.setShareIntent(getShareIntent());
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
                Tracker analyticsTracker = ((GaijinJobsApplication) getActivity().getApplication()).getTracker();
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_JOB_DETAIL,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_SHARE);
                return false;
            }
        });

        MenuItem bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        if (checkIfAlreadyBookmarked()) {
            bookmarkMenuItem.setIcon(R.drawable.ic_bookmark_highlighted);
            bookmarkMenuItem.setTitle(getString(R.string.action_cannot_bookmark));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Tracker analyticsTracker = ((GaijinJobsApplication) getActivity().getApplication()).getTracker();

        if (item.getItemId() == R.id.action_bookmark) {

            if (item.getTitle().equals(getString(R.string.action_can_bookmark))) {
                mCachePreferenceManager.addItemToCache(getActivity(), mJobEntity, PREFS_NAME_BOOKMARKS, KEY_BOOKMARKS);
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.addded_bookmark),
                        Toast.LENGTH_SHORT).show();

                item.setIcon(R.drawable.ic_bookmark_highlighted);
                item.setTitle(getString(R.string.action_cannot_bookmark));

                // fire analytics for bookmark count
                mAnalyticsHelper.sendTapEvent(analyticsTracker, GaijinJobsAnalyticsHelper.EVENT_CATEGORY_JOB_DETAIL,
                        GaijinJobsAnalyticsHelper.EVENT_LABEL_BOOKMARK);
            } else {
                mCachePreferenceManager.removeItemFromCache(getActivity(), mJobEntity, PREFS_NAME_BOOKMARKS, KEY_BOOKMARKS);
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.remove_bookmark),
                        Toast.LENGTH_SHORT).show();

                item.setIcon(R.drawable.ic_bookmark_white);
                item.setTitle(getString(R.string.action_can_bookmark));
            }

            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            if (!((JobDetailWebViewActivity) getActivity()).hasParentActivityMetadata()) {
                getActivity().finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_intent_subject));
        intent.putExtra(Intent.EXTRA_TEXT, mJobEntity.getJobUrl());

        return intent;
    }

    private boolean checkIfAlreadyBookmarked() {
        boolean isBookMarked = false;
        List<JobEntity> bookmarks = mCachePreferenceManager.getItemsFromCache(getActivity(), PREFS_NAME_BOOKMARKS, KEY_BOOKMARKS);
        if (bookmarks != null) {
            for (JobEntity jobEntity : bookmarks) {
                if (jobEntity.getJobUrl().equals(mJobEntity.getJobUrl())) {
                    isBookMarked = true;
                    break;
                }
            }
        }
        return isBookMarked;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // do nothing
    }

    @Override
    public void onDownMotionEvent() {
        // do nothing
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = ((JobDetailWebViewActivity) getActivity()).getActivityActionBar();
        if (ab == null) return;

        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }

    /**
     * try to make the browser go back in history if possible.
     *
     * @return true if browser went backed in history, false if it couldn't go back.
     */
    public boolean tryBrowserHistoryBack() {
        if (mWebView != null && mWebView.canGoBack() &&
                (mWebViewStatusView != null && !(mWebViewStatusView.getVisibility() == View.VISIBLE))) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public void refresh() {

        if (mWebView != null) {
            mStatusViewHelper.gone(mWebViewStatusView);
            mWebView.loadUrl(mJobEntity.getJobUrl());
        }
    }
}
