package jp.gaijins.jobs.common;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.gaijins.jobs.R;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class NavigationDrawerListAdapter extends ArrayAdapter<DrawerMenuItemIdentifier> {

    private Context mContext;
    private List<DrawerMenuItemIdentifier> mDrawerItemList;
    private int mLayoutResID;
    private int mSelectedItem;

    public NavigationDrawerListAdapter(Context context, int layoutResourceID,
                                       List<DrawerMenuItemIdentifier> listItems) {
        super(context, layoutResourceID, listItems);
        this.mContext = context;
        this.mLayoutResID = layoutResourceID;
        this.mDrawerItemList = listItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NavigationDrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            drawerHolder = new NavigationDrawerItemHolder();

            view = inflater.inflate(mLayoutResID, parent, false);
            drawerHolder.itemName = (TextView) view.findViewById(R.id.drawer_item_name);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (NavigationDrawerItemHolder) view.getTag();

        }

        DrawerMenuItemIdentifier drawerItem = this.mDrawerItemList.get(position);
        if (drawerItem != null) {
            drawerHolder.itemName.setText(drawerItem.getTitleResourceId());

            if (mSelectedItem == position) {
                drawerHolder.itemName.setTextColor(mContext.getResources().getColor(R.color.accent_color));
                drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(drawerItem.getIconResourceId(true)));
            } else {
                drawerHolder.itemName.setTextColor(mContext.getResources().getColor(R.color.primary_text_color));
                drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(drawerItem.getIconResourceId(false)));
            }
        }
        return view;
    }

    public static class NavigationDrawerItemHolder {
        TextView itemName;
        ImageView icon;
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }
}
