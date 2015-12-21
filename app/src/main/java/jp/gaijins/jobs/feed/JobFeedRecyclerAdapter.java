package jp.gaijins.jobs.feed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;

import java.util.List;

import jp.gaijins.jobs.R;
import jp.gaijins.jobs.common.GaijinJobsAnalyticsHelper;
import jp.gaijins.jobs.common.ImageHandler;
import jp.gaijins.jobs.entity.JobEntity;
import jp.gaijins.jobs.prefs.CachePreferenceManager;

/**
 * Created by nayak.vishal on 2015/12/14.
 */
public class JobFeedRecyclerAdapter extends RecyclerView.Adapter<JobFeedRecyclerAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG_BOOKMARKED = "bookmarked";
    private static final String TAG_NOT_BOOKMARKED = "not_bookmarked";

    private Context mContext;
    private List<JobEntity> items;
    private OnRecyclerViewItemClickListener<JobEntity> itemClickListener;
    private int itemLayout;
    private CachePreferenceManager mCachePreferenceManager;

    public JobFeedRecyclerAdapter(Context context, List<JobEntity> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
        this.mContext = context;
        mCachePreferenceManager = new CachePreferenceManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final JobEntity item = items.get(position);
        holder.itemView.setTag(item);

        if (item.getImageUrl() != null) {
            holder.articleImage.setImageBitmap(null);

            ImageHandler.getSharedInstance(holder.articleImage.getContext()).cancelRequest(holder.articleImage);
            ImageHandler.getSharedInstance(holder.articleImage.getContext()).load(item.getImageUrl()).fit().centerCrop()
                    .into(holder.articleImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            // do nothing
                        }

                        @Override
                        public void onError() {
                            // do nothing
                        }
                    });

            holder.articleImage.setVisibility(View.VISIBLE);
        }

        if (item.getJobDomainThumbnailUrl() != null) {
            holder.domainThumbnail.setImageBitmap(null);

            ImageHandler.getSharedInstance(holder.domainThumbnail.getContext()).cancelRequest(holder.domainThumbnail);
            ImageHandler.getSharedInstance(holder.domainThumbnail.getContext()).load(item.getJobDomainThumbnailUrl()).fit().centerCrop()
                    .into(holder.domainThumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            // do nothing
                        }

                        @Override
                        public void onError() {
                            // do nothing
                        }
                    });

            holder.domainThumbnail.setVisibility(View.VISIBLE);

        }

        if (item.getJobTitle() != null) {
            holder.articleTitle.setText(item.getJobTitle());
            holder.articleTitle.setVisibility(View.VISIBLE);
        }

        if (item.getJobDomainName() != null) {
            holder.domainName.setText(item.getJobDomainName());
            holder.domainName.setVisibility(View.VISIBLE);
        }

        holder.shareActionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).fireFeedAnalytics(GaijinJobsAnalyticsHelper.EVENT_LABEL_SHARE);
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.share_intent_subject);
                sendIntent.putExtra(Intent.EXTRA_TEXT, item.getJobUrl());
                holder.shareAction.getContext().startActivity(sendIntent);
            }
        });

        if (checkIfAlreadyBookmarked(item)) {
            holder.bookmarkAction.setImageResource(R.drawable.ic_bookmark_highlighted);
            holder.bookmarkAction.setTag(TAG_BOOKMARKED);
        } else {
            holder.bookmarkAction.setImageResource(R.drawable.ic_bookmark);
            holder.bookmarkAction.setTag(TAG_NOT_BOOKMARKED);
        }

        holder.bookmarkActionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tag = holder.bookmarkAction.getTag().toString();
                if (tag.equalsIgnoreCase(TAG_NOT_BOOKMARKED)) {
                    mCachePreferenceManager.addItemToCache(mContext, item, CachePreferenceManager.PREFS_NAME_BOOKMARKS, CachePreferenceManager.KEY_BOOKMARKS);
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.addded_bookmark),
                            Toast.LENGTH_SHORT).show();

                    holder.bookmarkAction.setTag(TAG_BOOKMARKED);
                    holder.bookmarkAction.setImageResource(R.drawable.ic_bookmark_highlighted);

                    // fire analytics to get bookmark count
                    if (mContext instanceof HomeActivity) {
                        ((HomeActivity) mContext).fireFeedAnalytics(GaijinJobsAnalyticsHelper.EVENT_LABEL_BOOKMARK);
                    }
                } else {
                    mCachePreferenceManager.removeItemFromCache(mContext, item, CachePreferenceManager.PREFS_NAME_BOOKMARKS, CachePreferenceManager.KEY_BOOKMARKS);
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.remove_bookmark),
                            Toast.LENGTH_SHORT).show();

                    holder.bookmarkAction.setTag(TAG_NOT_BOOKMARKED);
                    holder.bookmarkAction.setImageResource(R.drawable.ic_bookmark);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            JobEntity model = (JobEntity) view.getTag();
            itemClickListener.onItemClick(view, model);
        }
    }

    public void add(JobEntity item) {
        items.add(items.size(), item);
        notifyItemInserted(items.size());
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<JobEntity> listener) {
        this.itemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView articleImage;
        public TextView articleTitle;
        public ImageView domainThumbnail;
        public TextView domainName;
        public ImageView shareAction;
        public ImageView bookmarkAction;
        public FrameLayout shareActionContainer, bookmarkActionContainer;


        public ViewHolder(View itemView) {
            super(itemView);
            articleImage = (ImageView) itemView.findViewById(R.id.articleImage);
            articleTitle = (TextView) itemView.findViewById(R.id.articleTitle);
            domainThumbnail = (ImageView) itemView.findViewById(R.id.domainThumbnail);
            domainName = (TextView) itemView.findViewById(R.id.domainName);
            shareAction = (ImageView) itemView.findViewById(R.id.shareAction);
            bookmarkAction = (ImageView) itemView.findViewById(R.id.bookmarkAction);
            shareActionContainer = (FrameLayout) itemView.findViewById(R.id.shareActionContainer);
            bookmarkActionContainer = (FrameLayout) itemView.findViewById(R.id.bookmarkActionContainer);
        }
    }

    /*Checks whether a particular job item exists in SharedPreferences*/
    private boolean checkIfAlreadyBookmarked(JobEntity checkJobEntity) {
        boolean isBookMarked = false;
        List<JobEntity> bookmarks = mCachePreferenceManager.getItemsFromCache(mContext, CachePreferenceManager.PREFS_NAME_BOOKMARKS,
                CachePreferenceManager.KEY_BOOKMARKS);
        if (bookmarks != null) {
            for (JobEntity jobEntity : bookmarks) {
                if (jobEntity.getJobUrl().equals(checkJobEntity.getJobUrl())) {
                    isBookMarked = true;
                    break;
                }
            }
        }
        return isBookMarked;
    }
}
