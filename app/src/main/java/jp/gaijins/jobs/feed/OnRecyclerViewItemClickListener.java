package jp.gaijins.jobs.feed;

import android.view.View;

/**
 * Created by nayak.vishal on 2015/12/15.
 */
public interface OnRecyclerViewItemClickListener<Model> {
    public void onItemClick(View view, Model model);
}
