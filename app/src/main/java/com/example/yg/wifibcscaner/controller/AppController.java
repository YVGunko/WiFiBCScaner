package com.example.yg.wifibcscaner.controller;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.data.dto.MainActivityViews;

/**
 * Created by Shahbaz Hashmi on 2020-03-20.
 */
public class AppController extends Application {

    private static AppController mInstance;
    private Resources mResources;
    private DataBaseHelper mDBHelper;
    private SharedPreferences mSharedPreferences;
    private AlarmManager mAlarmManager;
    private MainActivityViews mMainActivityViews ;

    /**
     * init all required objects in onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mMainActivityViews = new MainActivityViews("","box","","div", "oper", "dep", "empl", "");
        mResources = getResources();
        mSharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mDBHelper = DataBaseHelper.getInstance(this);
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
    public MainActivityViews getMainActivityViews() { return mMainActivityViews; }
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public DataBaseHelper getDbHelper() {
        return mDBHelper;
    }
    public AlarmManager getAlarmManager() {
        return mAlarmManager;
    }
}
