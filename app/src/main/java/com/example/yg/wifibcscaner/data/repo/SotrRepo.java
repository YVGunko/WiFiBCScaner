package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Sotr;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.substring;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class SotrRepo {
    private static final String TAG = "sProject -> SotrRepo";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    public List<Sotr> getSotrIdByDivisionCodeAndOperationIdAndDepartmentId(String division_code, int operation_id, int department_id) {
        ArrayList<Sotr> list = new ArrayList<Sotr>();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT _id, Sotr FROM Sotr " +
                        "Where division_code=? and Id_o=? and Id_d=? Order by _id",
                new String [] {String.valueOf(division_code), String.valueOf(operation_id), String.valueOf(department_id)});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    list.add(new Sotr(cursor.getInt(0), cursor.getString(1)) );
                }
            }
            return list;
        }catch (Exception e) {
            Log.e(TAG, "getSotrIdByDivisionCodeAndOperationIdAndDepartmentId -> ".concat(e.getMessage()));
            return list;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public List<String> getAllSotrName(String code, int department_id, int operation_id) {
        ArrayList<String> nameDeps = new ArrayList<String>();

        try ( Cursor cursor = mDataBase.rawQuery("SELECT _id,tn_Sotr,Sotr FROM Sotr " +
                        "Where NOT expired and (((division_code=?) and (Id_o=?) and (Id_d=?))) or (_id=0) Order by _id",
                new String [] {String.valueOf(code), String.valueOf(operation_id), String.valueOf(department_id)}) ) {
            while (cursor.moveToNext()) {
                nameDeps.add(String.format("%s, %s", cursor.getString(2), cursor.getString(1)));
            }
            tryCloseCursor(cursor);
        }
        return nameDeps;
    }
    public Sotr getSotrReq(int Id_s){

        Sotr sotr = new Sotr(Id_s, "0",0,0);
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Id_o, Id_d, division_code FROM Sotr Where _id=?", new String [] {String.valueOf(Id_s)});
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                sotr.set_Id_o(cursor.getInt(0));
                sotr.set_Id_d(cursor.getInt(1));
                sotr.setDivision_code(cursor.getString(2));
            }
            tryCloseCursor(cursor);
            mDataBase.close();
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return sotr;
        }
    }
    public int getOneSotrIdByDepId(int depId){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT _id FROM Sotr WHERE Id_d=? LIMIT 1", new String [] {String.valueOf(depId)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getNameById -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int getSotr_id_by_Name(String nm){
        Cursor cursor = null;
        try {
            if( nm.indexOf(", ") > 0) {
                nm = substring(nm, nm.indexOf("0"), nm.length());
                cursor = mDataBase.rawQuery("SELECT _id FROM Sotr Where tn_Sotr='" + nm + "'", null);
                if (cursor != null && cursor.moveToFirst()){
                    return cursor.getInt(0);
                }
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getNameById -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int getId_sByOutDocId(String idOutDocs){
        int result = 0;

        Cursor cursor = mDataBase.rawQuery("SELECT distinct(Id_s) FROM Prods Where idOutDocs='" + idOutDocs + "'", null);
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getInt(0);
                cursor.moveToNext();
            }
        }
        tryCloseCursor(cursor);
        ////mDataBase.close();
        return result;
    }
    public String getNameById(int iD){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT tn_Sotr, Sotr FROM Sotr Where _id=?", new String [] {String.valueOf(iD)});
            if (cursor != null && cursor.moveToFirst()) {
                return String.format("%s %s", cursor.getString(1), cursor.getString(0));
            }
            return "";
        }catch (Exception e) {
            Log.e(TAG, "getNameById -> ".concat(e.getMessage()));
            return "";
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getSotrUpdateDate(@NonNull String globalUpdateDate){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Sotr", null);
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
    public long insertSotr(Sotr sotr) {
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Sotr.COLUMN_id, sotr.get_id());
            values.put(Sotr.COLUMN_Sotr, sotr.get_Sotr());
            values.put(Sotr.COLUMN_tn_Sotr, sotr.get_tn_Sotr());
            values.put(Sotr.COLUMN_DT, sDateTimeToLong(sotr.get_DT()));
            values.put(Sotr.COLUMN_Division_code, sotr.getDivision_code());
            values.put(Sotr.COLUMN_Id_d, sotr.get_Id_d());
            values.put(Sotr.COLUMN_Id_o, sotr.get_Id_o());
            values.put(Sotr.COLUMN_EXPIRED, sotr.isExpired());

            return mDataBase.insertWithOnConflict(Sotr.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
}
