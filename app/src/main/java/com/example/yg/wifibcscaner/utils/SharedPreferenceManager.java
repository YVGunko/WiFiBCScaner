package com.example.yg.wifibcscaner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.yg.wifibcscaner.BuildConfig;
import com.example.yg.wifibcscaner.controller.AppController;

import static android.content.Context.MODE_PRIVATE;
import static com.example.yg.wifibcscaner.Config.DEFAULT_UPDATE_DATE;

public class SharedPreferenceManager {

    private final String KEY_CACHE_TIMOUT = "cache_timeout";
    private final String NEXT_PAGE_TIMEOUT = "next_page_timeout";
    private final String NEXT_PAGE_TO_LOAD = "next_page_number";
    private final String UPDATE_DATE = "update_date";
    final static String VERSION_CODE = "version_code";
    final static String DB_NEED_REPLACE = "db_need_replace";
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
    public void setDbNeedReplace(boolean value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putBoolean(DB_NEED_REPLACE, value);
        editor.commit();
    }
    public boolean getDbNeedReplace() {
        return AppController.getInstance().getSharedPreferences().getBoolean(DB_NEED_REPLACE, DOESNT_EXIST == -1);
    }
    public void setCodeVersion(int value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putInt(VERSION_CODE, value);
        editor.commit();
    }
    public int getCodeVersion() {
        return AppController.getInstance().getSharedPreferences().getInt(VERSION_CODE, BuildConfig.VERSION_CODE);
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

    public void setNextPageToLoadAsInc() {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putInt(NEXT_PAGE_TO_LOAD, getCurrentPageToLoad()+1);
        editor.commit();
    }

    public int getCurrentPageToLoad() {
        return AppController.getInstance().getSharedPreferences().getInt(NEXT_PAGE_TO_LOAD, 0);
    }
    public String getUpdateDateString() {
        return AppController.getInstance().getSharedPreferences().getString(UPDATE_DATE, DEFAULT_UPDATE_DATE);
    }
}