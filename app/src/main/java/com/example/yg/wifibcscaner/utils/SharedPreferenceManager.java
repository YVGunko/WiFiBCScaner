package com.example.yg.wifibcscaner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.yg.wifibcscaner.BuildConfig;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;

import java.util.Date;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.*;

public class SharedPreferenceManager {

    private final String NEXT_DOWNLOAD_ATTEMPT_TIMEOUT = "download_timeout";
    private final String NEXT_PAGE_TO_LOAD = "next_page_number";
    private final String UPDATE_DATE = "update_date";
    final static String VERSION_CODE = "version_code";
    final static String DB_NEED_REPLACE = "db_need_replace";
    final static String LAST_SCANNED_BOX_DESCRIPTION = "last_scanned_box_description";
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
        editor.putLong(NEXT_DOWNLOAD_ATTEMPT_TIMEOUT, System.currentTimeMillis());
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
                NEXT_DOWNLOAD_ATTEMPT_TIMEOUT,
                0
        ) > BuildConfig.NEXT_DOWNLOAD_ATTEMPT_TIMEOUT
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
    public void setNextPageToLoadToZero() {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putInt(NEXT_PAGE_TO_LOAD, 0);
        editor.commit();
    }
    public int getCurrentPageToLoad() {
        return AppController.getInstance().getSharedPreferences().getInt(NEXT_PAGE_TO_LOAD, 0);
    }
    public long getUpdateDateLong() {
        return AppController.getInstance().getSharedPreferences().getLong(UPDATE_DATE, getStartOfDayLong(addDays(new Date(), -numberOfDaysInMonth(new Date()))));
    }
    public String getUpdateDateString() {
        return
                getDayTimeString(
                        AppController.getInstance().getSharedPreferences().getLong(UPDATE_DATE, getStartOfDayLong(addDays(new Date(), -numberOfDaysInMonth(new Date()))))
        );
    }

    public void setUpdateDateNow() {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(UPDATE_DATE, new Date().getTime() - 120);
        editor.commit();
    }
    public void setUpdateDate(long lDate) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(UPDATE_DATE, lDate);
        editor.commit();
    }
    public void setUpdateDate(String sDate) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(UPDATE_DATE, getDateTimeLong(sDate));
        editor.commit();
    }

    public void setLastScannedBoxDescription(String value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putString(LAST_SCANNED_BOX_DESCRIPTION, value);
        editor.commit();
    }
    public String getLastScannedBoxDescription() {
        return AppController.getInstance().getSharedPreferences().getString(LAST_SCANNED_BOX_DESCRIPTION,
                AppController.getInstance().getResourses().getString(R.string.no_data_to_view));
    }
}
