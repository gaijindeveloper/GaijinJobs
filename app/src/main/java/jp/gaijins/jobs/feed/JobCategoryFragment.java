package jp.gaijins.jobs.feed;

/**
 * Created by nayak.vishal on 2015/12/14.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Map;

import jp.gaijins.jobs.R;
import jp.gaijins.jobs.common.BootManager;
import jp.gaijins.jobs.detail.JobDetailWebViewActivity;
import jp.gaijins.jobs.entity.JobEntity;
import jp.gaijins.jobs.prefs.CachePreferenceManager;

/**
 * Fragment containing job feed.
 */
public class JobCategoryFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_CATEGORY_NUMBER = "category_number";
    private static final String EXTRA_JOB_ENTITY = "job_entity";

    private static final int CATEGORY_BOOKMARKS = 4;
    public static final String PREFS_NAME_BOOKMARKS = "jp.gaijins.jobs.PREFERENCE_BOOKMARKS";
    public static final String KEY_BOOKMARKS = "job_bookmarks";

    // html tags for parsing URLs
    private static final String KEY_TAG_META = "meta";
    private static final String KEY_TAG_PROPERTY = "property";
    private static final String KEY_TAG_CONTENT = "content";
    private static final String KEY_TAG_HREF = "href";
    private static final String KEY_TAG_OG_IMAGE = "og:image";
    private static final String KEY_TAG_OG_SITE_NAME = "og:site_name";
    private static final String REGEX_SITE_ICON = "link[href~=.*\\.(ico|png)]";
    private static final String REGEX_SITE_ICON_META = "meta[itemprop=image]";

    private int mCurrentCategoryId;
    private RecyclerView mRecyclerView;
    private ArrayList<JobEntity> mJobEntries;
    private JobFeedRecyclerAdapter mRecyclerViewAdapter;
    private ArrayList<ParseUrlAsyncTask> mAsyncTasks;
    private CachePreferenceManager mCachePreferenceManager;
    private BootManager mBootManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static JobCategoryFragment newInstance(int categoryNumber) {

        JobCategoryFragment fragment = new JobCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_NUMBER, categoryNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public JobCategoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJobEntries = new ArrayList<>();
        mAsyncTasks = new ArrayList<>();
        mCachePreferenceManager = new CachePreferenceManager();
        mBootManager = new BootManager(getActivity());

        fetchFeed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_activity_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.article_feed_list);
        mRecyclerViewAdapter = new JobFeedRecyclerAdapter(getActivity(), mJobEntries, R.layout.jobs_feed_item);
        mRecyclerViewAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<JobEntity>() {
            @Override
            public void onItemClick(View view, JobEntity jobEntity) {
                // open job detail WebView activity
                launchIntentForJobDetail(jobEntity);
            }
        });

        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setOnScrollListener(((HomeActivity) getActivity()).getRecyclerViewScrollListener());
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_color, R.color.accent_color, R.color.light_bg_interaction_color, R.color.light_bg_interaction_color);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshAndCheckForNewData();
                }
            });
        }

        return rootView;
    }

    private void fetchFeed() {
        if (mCurrentCategoryId == CATEGORY_BOOKMARKS) {
            // Bookmarks are always stored in cache
            getBookmarksFromCache();
        } else {
            // if first boot, fetch from network
            if (mBootManager.isFirstBoot()) {
                parseJobUrlList();
                mBootManager.storeFirstBootCompleted();
            } else {
                // try to fetch from cache first if not first boot
                if (!getCachedEntries()) {
                    // if no cache entries
                    parseJobUrlList();
                } else {
                    // if cache loading is done, check for new data as well
                    refreshAndCheckForNewData();
                }
            }
        }
    }

    // Parse job Url source using Jsoup
    private void parseJobUrlList() {
        Map<String, ArrayList<String>> jobUrlMap = ((HomeActivity) getActivity()).getCompleteJobSource();

        if (jobUrlMap == null || jobUrlMap.size() == 0) {
            Toast.makeText(getActivity(), "Sorry could not retrieve feed! Try again later", Toast.LENGTH_SHORT).show();
            return;
        }

        // for ALL
        if (mCurrentCategoryId == 0) {
            for (Map.Entry<String, ArrayList<String>> entry : jobUrlMap.entrySet()) {
                String jobType = entry.getKey();
                ArrayList<String> urlList = entry.getValue();
                for (String url : urlList) {
                    parseNewFeed(jobType, url, false);
                }
            }
        } else {

            // for Engineering, Design and Marketing
            for (Map.Entry<String, ArrayList<String>> entry : jobUrlMap.entrySet()) {
                String jobType = entry.getKey();
                if (!jobType.equals(getResources().
                        getStringArray(R.array.job_categories)[mCurrentCategoryId])) continue;

                ArrayList<String> urlList = entry.getValue();
                for (String url : urlList) {
                    parseNewFeed(jobType, url, false);
                }
            }
        }
    }

    private void parseNewFeed(String jobType, String url, boolean cacheOnly) {
        if (url == null || jobType == null) return;

        ParseUrlTaskParams params = new ParseUrlTaskParams(jobType, url, cacheOnly);
        ParseUrlAsyncTask task = new ParseUrlAsyncTask();
        task.execute(params);
        mAsyncTasks.add(task);
    }

    private void refreshAndCheckForNewData() {
        boolean isNewDataPresent = false;

        // check if URLs are already being pased, if yes return!
        for (ParseUrlAsyncTask asyncTask : mAsyncTasks) {
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                return;
            }
        }

        // get the current available job source
        Map<String, ArrayList<String>> jobUrlMap = ((HomeActivity) getActivity()).getCompleteJobSource();

        // get All cached entries
        ArrayList<JobEntity> cachedEntries = mCachePreferenceManager.getCacheByDrawerCategoryId(getActivity(), 0);

        if (jobUrlMap == null) {
            showSwipeRefreshIndicator(false);
            return;
        }

        // get new URLs for parsing, compare network and cache
        for (Map.Entry<String, ArrayList<String>> entry : jobUrlMap.entrySet()) {
            for (String url : entry.getValue()) {
                boolean itemAlreadyCached = false;

                for (JobEntity cachedEntry : cachedEntries) {
                    if (url.equals(cachedEntry.getJobUrl())) {
                        itemAlreadyCached = true;
                        break;
                    }
                }
                if (!itemAlreadyCached) {
                    parseNewFeed(entry.getKey(), url, true);
                    isNewDataPresent = true;
                }
            }
        }

        if (!isNewDataPresent) showSwipeRefreshIndicator(false);
    }

    private boolean getCachedEntries() {
        return setJobFeed(mCachePreferenceManager.getCacheByDrawerCategoryId(getActivity(), mCurrentCategoryId));
    }

    private void getBookmarksFromCache() {
        setJobFeed(mCachePreferenceManager.getItemsFromCache(getActivity(), PREFS_NAME_BOOKMARKS, KEY_BOOKMARKS));
    }

    private boolean setJobFeed(ArrayList<JobEntity> jobEntries) {
        if (jobEntries == null || jobEntries.size() == 0) {

            if (mCurrentCategoryId == CATEGORY_BOOKMARKS) {
                Toast.makeText(getActivity(), "Sorry no bookmarks yet!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Please wait. Fetching feed", Toast.LENGTH_LONG).show();
            }
            return false;
        }

        mJobEntries = jobEntries;
        return true;
    }

    private void showSwipeRefreshIndicator(boolean showProgress) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(showProgress);
        }
    }

    private void launchIntentForJobDetail(JobEntity jobEntity) {
        if (jobEntity == null) return;

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_JOB_ENTITY, jobEntity);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(getActivity(), JobDetailWebViewActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCurrentCategoryId = getArguments().getInt(ARG_CATEGORY_NUMBER);
        ((HomeActivity) activity).onSectionAttached(mCurrentCategoryId);
    }


    private class ParseUrlAsyncTask extends AsyncTask<ParseUrlTaskParams, Void, JobEntity> {

        String jobType;
        String jobUrl = null;
        boolean cacheOnly;

        String imageUrl = null;
        String jobTitle = null;
        String siteName = null;
        String siteIcon = null;

        @Override
        protected JobEntity doInBackground(ParseUrlTaskParams... params) {
            jobType = params[0].mJobType;
            jobUrl = params[0].mUrl;
            cacheOnly = params[0].mCacheOnly;

            try {
                Document doc = Jsoup.connect(jobUrl).get();
                // Get document (HTML page) title
                jobTitle = doc.title();

                Elements metaElems = doc.select(KEY_TAG_META);
                for (Element metaElem : metaElems) {
                    String propertyName = metaElem.attr(KEY_TAG_PROPERTY);
                    if (propertyName.equals(KEY_TAG_OG_IMAGE)) {
                        imageUrl = metaElem.attr(KEY_TAG_CONTENT);
                    }

                    if (propertyName.equals(KEY_TAG_OG_SITE_NAME)) {
                        siteName = metaElem.attr(KEY_TAG_CONTENT);
                    }
                }

                Element element = doc.head().select(REGEX_SITE_ICON).first();
                if (element == null) {
                    element = doc.head().select(REGEX_SITE_ICON_META).first();
                    if (element != null) {
                        siteIcon = element.attr(KEY_TAG_CONTENT);
                    }
                } else {
                    siteIcon = element.attr(KEY_TAG_HREF);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
            return new JobEntity(jobType, jobUrl, imageUrl, jobTitle, siteName, siteIcon);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JobEntity jobItem) {
            super.onPostExecute(jobItem);

            if (jobItem != null) {

                // new feed only to be cached, not displayed. Will be displayed on next app launch
                if (!cacheOnly) {
                    mRecyclerViewAdapter.add(jobItem);
                }
                mCachePreferenceManager.addItemToCache(getActivity(), jobItem,
                        mCachePreferenceManager.getCachePreferenceNameByJobType(jobType),
                        mCachePreferenceManager.getCachePreferenceKeyByJobType(jobType));
            }

            showSwipeRefreshIndicator(false);
        }
    }

    private static class ParseUrlTaskParams {
        String mJobType;
        String mUrl;
        boolean mCacheOnly;

        ParseUrlTaskParams(String jobType, String url, boolean cacheOnly) {
            this.mJobType = jobType;
            this.mUrl = url;
            this.mCacheOnly = cacheOnly;
        }
    }

    private void abortParseAsyncTasks() {
        for (ParseUrlAsyncTask task : mAsyncTasks) {
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
                task = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        abortParseAsyncTasks();
        mAsyncTasks = null;
        mJobEntries = null;
        super.onDestroy();
    }
}
