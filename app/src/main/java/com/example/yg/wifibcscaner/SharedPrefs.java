package com.example.yg.wifibcscaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefs {
    final static String PREFS_NAME = "WiFiBCScanerPrefsFile";
    final static String PREF_VERSION_CODE_KEY = "version_code";
    final static String PREF_DB_NEED_REPLACE = "db_need_replace";
    final static String FIRST_OPER_ONE = "first_oper_1";
    final static String FIRST_OPER_TWO = "first_oper_2";
    final static int DOESNT_EXIST = -1;
    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply(); // or editor.commit() in case you want to write data instantly
    }
    public static void setDefaults(String key, boolean value, Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        preferences.edit().putBoolean(key, value).apply(); // or editor.commit() in case you want to write data instantly
    }
    public static boolean getDefaults(String key, Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }
}
