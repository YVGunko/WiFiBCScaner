package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Division;

import java.util.ArrayList;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;

public class DivisionRepo {
    private static final String TAG = "sProject -> DivisionRepo";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    public List<String> getAllDivisionName() {
        ArrayList<String> list = new ArrayList<String>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT name FROM Division", null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0));
                }
            }
            return list;
        }catch (Exception e) {
            Log.e(TAG, "getAllDivisionName -> ".concat(e.getMessage()));
            return list;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getDivisionNameByCode(@NonNull String code){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT name FROM Division Where code=?", new String [] {code});
            if (cursor != null && cursor.moveToFirst()) {
                return String.format("%s", cursor.getString(0));
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "getDivisionNameByCode -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getDivisionsCodeByName(String name){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT code FROM Division Where name=?", new String [] {String.valueOf(name)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "getDivisionNameByCode -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public void insertDivisionInBulk(List<Division> list) {
        try {
            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO "+Division.TABLE+" (code, name) " +
                    " VALUES (?,?) ";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Division o : list) {
                statement.clearBindings();
                statement.bindString(1, o.getCode());
                statement.bindString(2, o.getName());

                statement.executeInsert();
            }
            mDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
        }
    }
}
