package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Deps;

import java.util.ArrayList;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class DepartmentRepo {
    private static final String TAG = "sProject -> DepartmentRepo.";
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
    public int getIdByOutDocCode(@NonNull String code){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Id_d FROM Prods Where idOutDocs=? LIMIT 1", new String [] {code});
            if (cursor != null && cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    return cursor.getInt(0);
                }
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getIdByOutDocCode -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public long insertDeps(Deps deps) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Deps.COLUMN_id, deps.get_id());
            values.put(Deps.COLUMN_Id_deps, deps.get_Id_deps());
            values.put(Deps.COLUMN_Name_Deps, deps.get_Name_Deps());
            values.put(Deps.COLUMN_DT, sDateTimeToLong(deps.get_DT()));
            values.put(Deps.COLUMN_Division_code, deps.getDivision_code());
            values.put(Deps.COLUMN_Id_o, deps.get_Id_o());

            return mDataBase.insertWithOnConflict(Deps.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
}
