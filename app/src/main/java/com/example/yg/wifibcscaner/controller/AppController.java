package com.example.yg.wifibcscaner.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.service.SharedPrefs;

public class AppController extends Application {

    private static AppController mInstance;
    private Resources mResources;
    private DataBaseHelper mDBHelper;
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    /**
     * init all required objects in onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = getApplicationContext();
        mResources = getResources();
        mSharedPreferences = getSharedPreferences(SharedPrefs.getPrefsName(), Context.MODE_PRIVATE);
        mDBHelper = DataBaseHelper.getInstance();
    }

    /**
     * singleton of AppController
     *
     * @return AppController instance
     */
    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public Resources getResourses() {
        return mResources;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public DataBaseHelper getDbHelper() {
        return mDBHelper;
    }

    public Context getContext() {
        return mContext;
    }
}
