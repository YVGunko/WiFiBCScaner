package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;

import java.util.ArrayList;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class DepartmentRepo {
    private static final String TAG = "sProject -> DepartmentRepo";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    public List<String> getAllDepartmentNameByDivisionCodeAndOperationId(String code, int iD) {
        ArrayList<String> nameDeps = new ArrayList<String>();
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id,Id_deps,Name_Deps,division_code,Id_o FROM Deps " +
                    "where (((division_code=?)or(division_code=0)) AND ((Id_o=?)or(Id_o=0))) Order by _id", new String [] {String.valueOf(code), String.valueOf(iD)});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    nameDeps.add(cursor.getString(2));
                }
            }
            return nameDeps;
        }catch (Exception e) {
            Log.e(TAG, "getAllDepartmentNameByDivisionCodeAndOperationId -> ".concat(e.getMessage()));
            return nameDeps;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getDepNameById(int iD){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Name_Deps FROM Deps Where _id=?", new String [] {String.valueOf(iD)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "getDepNameById -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int getDepIdByName(@NonNull String nm){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT _id FROM Deps Where Name_Deps=?", new String [] {nm});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getDepIdByName -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getDepUpdateDate(@NonNull String globalUpdateDate){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Deps", null);
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
    public List<Integer> getAllDepartmentIdByDivisionCodeAndOperationId(@NonNull String code, @NonNull int iD) {
        ArrayList<Integer> result = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = mDataBase.rawQuery("SELECT _id,Id_deps,Name_Deps,DT,division_code,Id_o FROM Deps " +
                    " where division_code=? AND Id_o=? ", new String [] {code, String.valueOf(iD)});

            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getInt(0));
                }
            }
            return result;
        }catch (Exception e) {
            Log.e(TAG, "getAllDepartmentNameByDivisionCodeAndOperationId -> ".concat(e.getMessage()));
            return result;
        } finally {
            tryCloseCursor(cursor);
        }
    }
}
