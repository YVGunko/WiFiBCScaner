package com.example.yg.wifibcscaner.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.addDays;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getStartOfDayLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.numberOfDaysInMonth;

public class SharedPrefs {
    private static final String TAG = "SharedPreferenceMan";

    final static String PREFS_NAME = "WiFiBCScanerPrefsFile";
    final static String PREF_VERSION_CODE_KEY = "version_code";
    final static String PREF_DB_NEED_REPLACE = "db_need_replace";
    final static String FIRST_OPER_ONE = "first_oper_1";
    final static String FIRST_OPER_TWO = "first_oper_2";
    final static String OUTDOCS_DAYS = "outDocsDays";
    final static String OUTDOCS_NUMERATION_START_DATE = "outDocsNumStartDate";
    final static String LAST_SCANNED_ORDER_DESCRIPTION = "last_scanned_order_description";
    final static String LAST_SCANNED_BOX_DESCRIPTION = "last_scanned_box_description";
    private final String NEXT_DOWNLOAD_ATTEMPT_TIME = "download_timeout";
    private final String NEXT_PAGE_TO_LOAD = "next_page_number";
    private final String UPDATE_DATE = "update_date";
    final static int TIME_SHIFT = 300000;
    final static int DOESNT_EXIST = -1;

    private SharedPreferences sharedPref;
    private Context appContext;

    private static SharedPrefs instance;
    SharedPreferences.Editor editor;

    public static synchronized SharedPrefs getInstance(Context applicationContext){
        if(instance == null)
            instance = new SharedPrefs(applicationContext);
        return instance;
    }

    private SharedPrefs(Context applicationContext) {
        appContext = applicationContext;
        sharedPref = appContext.getSharedPreferences(
                "WiFiBCScanerPrefsFile", Context.MODE_PRIVATE );
    }
    public void setDbNeedReplace(boolean value) {
        editor = sharedPref.edit();
        editor.putBoolean(PREF_DB_NEED_REPLACE, value);
        editor.apply();
    }
    public boolean getDbNeedReplace() {
        return sharedPref.getBoolean(PREF_DB_NEED_REPLACE, false);
    }
    public void setCodeVersion(int value) {
        editor = sharedPref.edit();
        editor.putInt(PREF_VERSION_CODE_KEY, value);
        editor.apply();
    }
    public int getCodeVersion() {
        return sharedPref.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
    }
    public void setOutDocsDays(int value) {
        editor = sharedPref.edit();
        editor.putInt(OUTDOCS_DAYS, value);
        editor.apply();
    }
    public int getOutDocsDays() {
        return sharedPref.getInt(OUTDOCS_DAYS, 1);
    }
    public void setOutdocsNumerationStartDate(long value) {
        editor = sharedPref.edit();
        editor.putLong(OUTDOCS_NUMERATION_START_DATE, value);
        editor.apply();
    }
    /*
    * I'd like to return first date of year as default or date that was set manually in case it's latter then first.
    * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getOutdocsNumerationStartDate() {
        long result = sharedPref.getLong(OUTDOCS_NUMERATION_START_DATE, DateTimeUtils.getFirstDayOfYear());
        return ( result > DateTimeUtils.getFirstDayOfYear() & result <= new Date().getTime() ) ? result : DateTimeUtils.getFirstDayOfYear();
    }
    /* data load */
    public void setNextPageToLoadToZero() {
        editor = sharedPref.edit();
        editor.putInt(NEXT_PAGE_TO_LOAD, 0);
        editor.commit();
    }
    public int getCurrentPageToLoad() {
        return sharedPref.getInt(NEXT_PAGE_TO_LOAD, 0);
    }
    public void setNextPageToLoadAsInc() {
        editor = sharedPref.edit();
        editor.putInt(NEXT_PAGE_TO_LOAD, getCurrentPageToLoad()+1);
        editor.commit();
    }
    public String getUpdateDateString() {
        return
                getDayTimeString(
                        sharedPref.getLong(UPDATE_DATE, getStartOfDayLong(addDays(new Date(), -numberOfDaysInMonth(new Date()))))
                );
    }
    public void setUpdateDateNow() {
        editor = sharedPref.edit();
        editor.putLong(UPDATE_DATE, new Date().getTime() - TIME_SHIFT);
        editor.commit();
    }
    public void setLastUpdatedTimestamp() {
        editor = sharedPref.edit();
        editor.putLong(NEXT_DOWNLOAD_ATTEMPT_TIME, System.currentTimeMillis());
        Log.d(TAG, " setLastUpdatedTimestamp -> "+DateTimeUtils.getFormattedDateTime(System.currentTimeMillis()));
        editor.commit();
    }
}
