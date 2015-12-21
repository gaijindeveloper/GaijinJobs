package jp.gaijins.jobs.common;

import java.util.ArrayList;
import java.util.List;

import jp.gaijins.jobs.R;

/**
 * Created by nayak.vishal on 2015/12/09.
 */
public enum DrawerMenuItemIdentifier {
    ALL(R.drawable.ic_menu_all, R.drawable.ic_menu_all_highlighted,
            R.string.title_category_all),
    ENGINEEEING(R.drawable.ic_menu_all, R.drawable.ic_menu_all_highlighted,
            R.string.title_category_engineering),
    DESIGN(R.drawable.ic_menu_all, R.drawable.ic_menu_all_highlighted,
            R.string.title_category_design),
    MARKETING(R.drawable.ic_menu_all, R.drawable.ic_menu_all_highlighted,
            R.string.title_category_marketing),
    BOOKMARKS(R.drawable.ic_bookmark, R.drawable.ic_bookmark_highlighted,
            R.string.title_category_bookmarks),
    UNKNOWN(-1, -1, -1);

    private final int mIconResourceId;
    private final int mItemSelectedIconResourceId;
    private final int mTitleResourceId;

    private DrawerMenuItemIdentifier(int iconResourceId, int itemSelectedIconResourceId,
                                     int titleResourceId) {
        mIconResourceId = iconResourceId;
        mItemSelectedIconResourceId = itemSelectedIconResourceId;
        mTitleResourceId = titleResourceId;
    }

    public static List<DrawerMenuItemIdentifier> getAllDrawerMenuListItems() {
        List<DrawerMenuItemIdentifier> drawerMenuList = new ArrayList<>();

        for (DrawerMenuItemIdentifier itemType : values()) {
            if (itemType.equals(DrawerMenuItemIdentifier.UNKNOWN)) continue;
            drawerMenuList.add(itemType);
        }
        return drawerMenuList;
    }

    public int getIconResourceId(boolean isSelected) {
        if (isSelected) {
            return mItemSelectedIconResourceId;
        } else {
            return mIconResourceId;
        }
    }

    public int getTitleResourceId() {
        return mTitleResourceId;
    }
}
