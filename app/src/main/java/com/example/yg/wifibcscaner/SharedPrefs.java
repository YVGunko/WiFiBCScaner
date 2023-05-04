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
    final static String OUTDOCS_DAYS = "outDocsDays";
    final static int DOESNT_EXIST = -1;

    private SharedPreferences sharedPref;
    private Context appContext;

    private static SharedPrefs instance;

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
/*/
    public void writeData(float value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(appContext.getString(R.string.key_data), value);
        editor.apply();
    }

    public float readData() {
        return sharedPref.getFloat(appContext.getString(R.string.key_data), 0);
    }
*/
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
    public void setDbNeedReplace(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_DB_NEED_REPLACE, value);
        editor.apply();
    }
    public boolean getDbNeedReplace() {
        return sharedPref.getBoolean(PREF_DB_NEED_REPLACE, false);
    }
    public void setCodeVersion(int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_VERSION_CODE_KEY, value);
        editor.apply();
    }
    public int getCodeVersion() {
        return sharedPref.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
    }
    public void setOutDocsDays(int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(OUTDOCS_DAYS, value);
        editor.apply();
    }
    public int getOutDocsDays() {
        return sharedPref.getInt(OUTDOCS_DAYS, 1);
    }
}
