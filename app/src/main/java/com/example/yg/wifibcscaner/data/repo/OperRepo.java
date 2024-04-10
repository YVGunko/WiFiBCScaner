package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class OperRepo {
    private static final String TAG = "sProject -> OperRepo";
    private SQLiteDatabase mDataBase ;

    public List<String> getAllOperNameByDivisionCode(@NonNull String division_code) {
        ArrayList<String> nameDeps = new ArrayList<String>(Collections.singleton("Выберите операцию"));
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id,Opers FROM Opers"+
                    " Where (division_code=?)or(division_code=0) Order by _id", new String [] {String.valueOf(division_code)});
            while (cursor.moveToNext()) {
                nameDeps.add(cursor.getString(1));
            }
            return nameDeps;
        }catch (Exception e) {
            Log.e(TAG, "getAllnameOpers -> ".concat(e.getMessage()));
            return nameDeps;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    public String getOperNameById(@NonNull int iD){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT Opers FROM Opers Where _id="+ iD, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "getOperNameById -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    public int getOperIdByName(@NonNull String nm){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id FROM Opers Where Opers=?", new String [] {String.valueOf(nm)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getOperNameById -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public String getOperUpdateDate(@NonNull String globalUpdateDate){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Opers", null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) > sDateTimeToLong(globalUpdateDate) ? cursor.getLong(0) : sDateTimeToLong(globalUpdateDate));
            }
            return globalUpdateDate;
        }catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public long insertOpers(@NonNull List<Operation> list) {
        long counter = 0L;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            for (Operation oper: list) {
                values.clear();
                values.put(Operation.COLUMN_id, oper.get_id());
                values.put(Operation.COLUMN_Opers, oper.get_Opers());
                values.put(Operation.COLUMN_DT, sDateTimeToLong(oper.get_dt()));
                values.put(Operation.COLUMN_Division, oper.getDivision_code());

                counter +=  mDataBase.insertWithOnConflict(Operation.TABLE, null, values, 5);
            }
            return counter;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        } finally {
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
}
