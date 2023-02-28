package com.example.yg.wifibcscaner.controller;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.example.yg.wifibcscaner.DataBaseHelper;

/**
 * Created by Shahbaz Hashmi on 2020-03-20.
 */
public class AppController extends Application {

    private static AppController mInstance;
    private Resources mResources;
    private DataBaseHelper mDBHelper;
    private SharedPreferences mSharedPreferences;
    private AlarmManager mAlarmManager;

    /**
     * init all required objects in onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mResources = getResources();
        mDBHelper = DataBaseHelper.getInstance(this);
        mSharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
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

    public DataBaseHelper getDbHelper() {
        return mDBHelper;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public AlarmManager getAlarmManager() {
        return mAlarmManager;
    }
}