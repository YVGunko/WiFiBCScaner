package com.example.yg.wifibcscaner.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.service.SharedPrefs;

public class AppController extends Application {

    private static AppController mInstance;
    private Resources mResources;
    private DataBaseHelper mDBHelper;
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private OutDocs currentOutDoc;
    private Defs defs;
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
        currentOutDoc = new OutDocs();
        defs = new Defs();
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

    public OutDocs getCurrentOutDoc() {
        return currentOutDoc;
    }
    public void setCurrentOutDoc(OutDocs outDoc) {
        this.currentOutDoc = outDoc;
    }
    public Defs getDefs() {
        return this.defs;
    }
    public void setDefs(Defs defs) {
        this.defs = defs;
    }
}
