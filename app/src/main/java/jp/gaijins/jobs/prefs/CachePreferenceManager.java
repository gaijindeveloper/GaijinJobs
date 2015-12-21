package jp.gaijins.jobs.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.gaijins.jobs.entity.JobEntity;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class CachePreferenceManager {

    private static final String TAG = CachePreferenceManager.class.getSimpleName();

    private static final String CATEGORY_ENGINEERING = "Engineering";
    private static final String CATEGORY_DESIGN = "Design";
    private static final String CATEGORY_MARKETING = "Marketing";
    private static final String CATEGORY_BOOKMARKS = "Bookmarks";

    public static final String PREFS_NAME_ENGINEERING = "jp.gaijins.jobs.PREFERENCE_ENGINEERING";
    public static final String PREFS_NAME_DESIGN = "jp.gaijins.jobs.PREFERENCE_DESIGN";
    public static final String PREFS_NAME_MARKETING = "jp.gaijins.jobs.PREFERENCE_MARKETING";
    public static final String PREFS_NAME_BOOKMARKS = "jp.gaijins.jobs.PREFERENCE_BOOKMARKS";

    public static final String KEY_ENGINEERING = "job_engineering";
    public static final String KEY_DESIGN = "job_design";
    public static final String KEY_MARKETING = "job_marketing";
    public static final String KEY_BOOKMARKS = "job_bookmarks";

    public CachePreferenceManager() {
        super();
    }

    public void saveItemsToCache(Context context, ArrayList<JobEntity> cachedItems, String preferenceName, String key) {
        if (context == null || cachedItems == null || preferenceName == null || key == null) return;

        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonCachedItems = gson.toJson(cachedItems);

        editor.putString(key, jsonCachedItems);
        editor.commit();
    }

    public void addItemToCache(Context context, JobEntity jobEntity, String preferenceName, String key) {
        if (jobEntity == null || preferenceName == null || key == null) return;

        ArrayList<JobEntity> cachedItems = getItemsFromCache(context, preferenceName, key);

        // don't add item if already present
        if (cachedItems != null && cachedItems.contains(jobEntity)) return;

        if (cachedItems == null) cachedItems = new ArrayList<>();
        cachedItems.add(0, jobEntity);
        saveItemsToCache(context, cachedItems, preferenceName, key);
    }

    public void removeItemFromCache(Context context, JobEntity jobEntity, String preferenceName, String key) {
        if (jobEntity == null || preferenceName == null || key == null) return;

        ArrayList<JobEntity> cachedItems = getItemsFromCache(context, preferenceName, key);
        if (cachedItems != null) {
            ArrayList<JobEntity> updatedCacheItems = new ArrayList<>();
            // for some reason ArrayList remove doesn't work, do doing it this way
            for (JobEntity existingCacheItem : cachedItems) {
                if (existingCacheItem.getJobUrl().equals(jobEntity.getJobUrl())) continue;
                updatedCacheItems.add(existingCacheItem);
            }

            saveItemsToCache(context, updatedCacheItems, preferenceName, key);
        }
    }

    public ArrayList<JobEntity> getItemsFromCache(Context context, String preferenceName, String key) {
        if (context == null || preferenceName == null || key == null) return null;

        SharedPreferences settings;
        List<JobEntity> cachedItems;

        settings = context.getSharedPreferences(preferenceName,
                Context.MODE_PRIVATE);

        if (settings.contains(key)) {
            String jsonFavorites = settings.getString(key, null);
            Gson gson = new Gson();
            JobEntity[] favoriteItems = gson.fromJson(jsonFavorites,
                    JobEntity[].class);

            cachedItems = Arrays.asList(favoriteItems);
            cachedItems = new ArrayList<>(cachedItems);
        } else {
            return null;
        }

        return (cachedItems.size() > 0) ? (ArrayList<JobEntity>) cachedItems : null;
    }

    public String getCachePreferenceNameByJobType(String jobType) {
        if (jobType == null) return null;

        switch (jobType) {
            case CATEGORY_ENGINEERING:
                return PREFS_NAME_ENGINEERING;
            case CATEGORY_DESIGN:
                return PREFS_NAME_DESIGN;
            case CATEGORY_MARKETING:
                return PREFS_NAME_MARKETING;
            case CATEGORY_BOOKMARKS:
                return PREFS_NAME_BOOKMARKS;
            default:
                Log.e(TAG, "Illegal category type");
                return null;
        }
    }

    public String getCachePreferenceKeyByJobType(String jobType) {
        if (jobType == null) return null;

        switch (jobType) {
            case CATEGORY_ENGINEERING:
                return KEY_ENGINEERING;
            case CATEGORY_DESIGN:
                return KEY_DESIGN;
            case CATEGORY_MARKETING:
                return KEY_MARKETING;
            case CATEGORY_BOOKMARKS:
                return KEY_BOOKMARKS;
            default:
                Log.e(TAG, "Illegal category type");
                return null;
        }
    }

    public ArrayList<JobEntity> getCacheByDrawerCategoryId(Context context, int categoryId) {
        ArrayList<JobEntity> cachedEntries = new ArrayList<>();

        switch (categoryId) {
            case 0:
                if (getItemsFromCache(context, PREFS_NAME_ENGINEERING, KEY_ENGINEERING) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_ENGINEERING, KEY_ENGINEERING));
                }

                if (getItemsFromCache(context, PREFS_NAME_DESIGN, KEY_DESIGN) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_DESIGN, KEY_DESIGN));
                }

                if (getItemsFromCache(context, PREFS_NAME_MARKETING, KEY_MARKETING) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_MARKETING, KEY_MARKETING));
                }
                return cachedEntries;
            case 1:
                if (getItemsFromCache(context, PREFS_NAME_ENGINEERING, KEY_ENGINEERING) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_ENGINEERING, KEY_ENGINEERING));
                }
                return cachedEntries;
            case 2:
                if (getItemsFromCache(context, PREFS_NAME_DESIGN, KEY_DESIGN) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_DESIGN, KEY_DESIGN));
                }
                return cachedEntries;
            case 3:
                if (getItemsFromCache(context, PREFS_NAME_MARKETING, KEY_MARKETING) != null) {
                    cachedEntries.addAll(getItemsFromCache(context, PREFS_NAME_MARKETING, KEY_MARKETING));
                }
                return cachedEntries;
            default:
                Log.e(TAG, "Illegal job type");
                return null;
        }
    }
}

