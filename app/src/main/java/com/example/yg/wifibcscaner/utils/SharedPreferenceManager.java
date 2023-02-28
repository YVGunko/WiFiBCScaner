package com.example.yg.wifibcscaner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.yg.wifibcscaner.BuildConfig;
import com.example.yg.wifibcscaner.controller.AppController;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferenceManager {

    private final String KEY_CACHE_TIMOUT = "cache_timeout";
    private final String NEXT_PAGE_TIMEOUT = "next_page_timeout";
    private final String NEXT_PAGE_TO_LOAD = "next_page_number";
    private final String UPDATE_DATE = "update_date";
    final static String PREF_VERSION_CODE_KEY = "version_code";
    final static String PREF_DB_NEED_REPLACE = "db_need_replace";
    final static int DOESNT_EXIST = -1;

    private static SharedPreferenceManager sharedPreferenceManager;

    public static SharedPreferenceManager getInstance() {
        if (sharedPreferenceManager == null) {
            sharedPreferenceManager = new SharedPreferenceManager();
        }
        return sharedPreferenceManager;
    }

    SharedPreferences.Editor editor;

    public void setLastUpdatedTimestamp() {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(KEY_CACHE_TIMOUT, System.currentTimeMillis());
        editor.commit();
    }
    public void setNextPageTimestamp() {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(NEXT_PAGE_TIMEOUT, System.currentTimeMillis());
        editor.commit();
    }
    public void setDefaults(String key, String value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }
    public void setDefaults(String key, boolean value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public boolean getDefaults(String key) {
        return AppController.getInstance().getSharedPreferences().getBoolean(key, false);
    }
    /**
     * logic to check if cache data expired
     */
    public boolean isLocalDataExpired() {
        if (System.currentTimeMillis() - AppController.getInstance().getSharedPreferences().getLong(
                KEY_CACHE_TIMOUT,
                0
        ) > BuildConfig.CACHE_TIMEOUT
        ) {
            return true;
        }
        return false;
    }

}
