package jp.gaijins.jobs.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class BootManager {

    private static final String PREFERENCE_KEY_IS_FIRST_BOOT = "isFirstBoot";
    private SharedPreferences mPreferences;

    private Context mContext;

    /**
     * @param context : application context
     */
    public BootManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public boolean isFirstBoot() {
        SharedPreferences p = getPreferences();
        return p.getBoolean(PREFERENCE_KEY_IS_FIRST_BOOT, true);
    }

    /**
     * Set boot flag as false to indicate first boot is completed.
     */
    public void storeFirstBootCompleted() {
        SharedPreferences p = getPreferences();
        SharedPreferences.Editor editor = p.edit();
        editor.putBoolean(PREFERENCE_KEY_IS_FIRST_BOOT, false);
        editor.commit();
    }

    public SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        }
        return mPreferences;
    }
}
