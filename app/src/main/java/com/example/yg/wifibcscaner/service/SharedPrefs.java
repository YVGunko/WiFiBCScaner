package com.example.yg.wifibcscaner.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefs {
    final static String PREFS_NAME = "WiFiBCScanerPrefsFile";
    final static String PREF_VERSION_CODE_VERSION = "version_code";
    final static String PREF_DB_NEED_REPLACE = "db_need_replace";
    final static String OUTDOCS_DAYS = "outDocsDays";
    final static String OUTDOCS_NUMERATION_START_DATE = "outDocsNumStartDate";
    final static int DOESNT_EXIST = -1;

    private static SharedPrefs sharedPref;
    SharedPreferences.Editor editor;

    public static synchronized SharedPrefs getInstance(){
        if (sharedPref == null) {
            sharedPref = new SharedPrefs();
        }
        return sharedPref;
    }

    public void setDbNeedReplace(boolean value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putBoolean(PREF_DB_NEED_REPLACE, value);
        editor.apply();
    }
    public boolean getDbNeedReplace() {
        return AppController.getInstance().getSharedPreferences().getBoolean(PREF_DB_NEED_REPLACE, false);
    }
    public void setCodeVersion(int value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putInt(PREF_VERSION_CODE_VERSION, value);
        editor.apply();
    }
    public int getCodeVersion() {
        return AppController.getInstance().getSharedPreferences().getInt(PREF_VERSION_CODE_VERSION, DOESNT_EXIST);
    }
    public void setOutDocsDays(int value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putInt(OUTDOCS_DAYS, value);
        editor.apply();
    }
    public int getOutDocsDays() {
        return AppController.getInstance().getSharedPreferences().getInt(OUTDOCS_DAYS, 1);
    }
    public void setOutdocsNumerationStartDate(long value) {
        editor = AppController.getInstance().getSharedPreferences().edit();
        editor.putLong(OUTDOCS_NUMERATION_START_DATE, value);
        editor.apply();
    }
    /*
    * I'd like to return first date of year as default or date that was set manually in case it's latter then first.
    * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getOutdocsNumerationStartDate() {
        long result = AppController.getInstance().getSharedPreferences().getLong(OUTDOCS_NUMERATION_START_DATE, DateTimeUtils.getFirstDayOfYear());
        return ( result > DateTimeUtils.getFirstDayOfYear() & result <= new Date().getTime() ) ? result : DateTimeUtils.getFirstDayOfYear();
    }
}
