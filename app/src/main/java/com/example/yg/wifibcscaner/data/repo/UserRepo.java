package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class UserRepo {
    private static final String TAG = "sProject -> UserRepo";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    public String getUserUpdateDate(@NonNull String globalUpdateDate){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM user", null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) > sDateTimeToLong(globalUpdateDate) ? cursor.getLong(0) : sDateTimeToLong(globalUpdateDate));
            }
            return globalUpdateDate;
        }catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        } finally {
            tryCloseCursor(cursor);
        }
    }

    public boolean checkSuperUser (int _id) {
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT superUser FROM user Where _id=?",
                    new String [] {String.valueOf(_id)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) != 0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkSuperUser -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }
}
