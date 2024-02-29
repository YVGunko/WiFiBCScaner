package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.user;

import java.util.ArrayList;
import java.util.List;

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
    public String getUserName(int code){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT name FROM user Where _id=?", new String [] {String.valueOf(code)});
            if (cursor != null && cursor.moveToFirst()) {
                return String.format("%s", cursor.getString(0));
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "checkSuperUser -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
        }
    }

    public int getUserSotrById(int _id){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Id_s FROM user Where _id=?", new String [] {String.valueOf(_id)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "checkSuperUser -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public Boolean checkUserPswdById(int id, String pswd){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id FROM user Where _id=? and pswd=?",
                    new String [] {String.valueOf(id), String.valueOf(pswd)});
            if ((cursor != null) && cursor.moveToFirst()) {
                return cursor.getInt(0) != 0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkUserPswdById -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public List<String> getAllUserName() {
        ArrayList<String> alUserName = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT name FROM user WHERE _id<>0 and NOT expired order by name", null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    alUserName.add(cursor.getString(0));
                }
            }
            return alUserName;
        }catch (Exception e) {
            Log.e(TAG, "getAllUserName -> ".concat(e.getMessage()));
            return alUserName;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int getUserIdByName(String nm) {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id FROM user Where name='" + nm + "'", null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getUserIdByName -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public boolean checkIfUserTableEmpty () {
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT count(*) FROM user Where _id<>0",
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0)==0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkIfUserTableEmpty -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public long insertUser(user user) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_id, user.get_id());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_Id_s, user.get_Id_s());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_name, user.getName());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_pswd, user.getPswd());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_DT, sDateTimeToLong(user.get_DT()));
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_superUser, user.isSuperUser());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_EXPIRED, user.isExpired());

            return mDataBase.insertWithOnConflict(com.example.yg.wifibcscaner.data.model.user.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
}
