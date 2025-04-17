package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.substring;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getTodayMorning;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class SotrRepo {
    private static final String TAG = "sProject -> SotrRepo";
    private SQLiteDatabase mDataBase ;

    public SotrDownloadListenner listenner;
    public interface SotrDownloadListenner {
        void onSuccess();

        void onFail(Throwable t);
    }
    public void setListenner(SotrDownloadListenner listenner) {
        this.listenner = listenner;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void downloadSotr() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getSotr()
                    .enqueue(new Callback<List<Sotr>>() {
                        @Override
                        public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {
                            if (response.isSuccessful()  && !response.body().isEmpty())
                                insertSotrInBulk(response.body());
                        }

                        private void insertSotrInBulk(List<Sotr> list) {
                            try {
                                mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                                mDataBase.beginTransaction();
                                String sql = "INSERT OR REPLACE INTO "+Sotr.TABLE+" (_id, tn_Sotr, sotr, dt, Id_d, Id_o, division_code, expired) " +
                                        " VALUES (?,?,?,?,?,?,?,?) ";

                                SQLiteStatement statement = mDataBase.compileStatement(sql);

                                for (Sotr o : list) {
                                    statement.clearBindings();
                                    statement.bindLong(1, o.get_id());
                                    statement.bindString(2, o.get_tn_Sotr());
                                    statement.bindString(3, o.get_Sotr());
                                    statement.bindString(4, o.get_DT());
                                    statement.bindLong(5, o.get_Id_d());
                                    statement.bindLong(6, o.get_Id_o());
                                    statement.bindString(7, o.getDivision_code());
                                    statement.bindLong(8, o.getExpiredAsLong());

                                    statement.executeInsert();
                                }
                                mDataBase.setTransactionSuccessful();
                                if (listenner != null) listenner.onSuccess();
                            } catch (Exception e) {
                                Log.w(TAG, e);
                                throw new RuntimeException("Загрузка данных. Исключительная ситуация при добавлении сотрудников.");
                            } finally {
                                mDataBase.endTransaction();
                                AppController.getInstance().getDbHelper().closeDataBase();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Sotr>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                            if (listenner != null) listenner.onFail(t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
        }
        });
        return;
    }
    public List<Sotr> getSotrIdByDivisionCodeAndOperationIdAndDepartmentId(String division_code, int operation_id, int department_id) {
        ArrayList<Sotr> list = new ArrayList<Sotr>();
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id, Sotr FROM Sotr " +
                        "Where NOT expired and division_code=? and Id_o=? and Id_d=? Order by _id",
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public List<String> getAllSotrName(String code, int department_id, int operation_id) {
        ArrayList<String> nameDeps = new ArrayList<String>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try ( Cursor cursor = mDataBase.rawQuery("SELECT _id,tn_Sotr,Sotr FROM Sotr " +
                        "Where NOT expired and (((division_code=?) and (Id_o=?) and (Id_d=?))) or (_id=0) Order by _id",
                new String [] {String.valueOf(code), String.valueOf(operation_id), String.valueOf(department_id)}) ) {
            while (cursor.moveToNext()) {
                nameDeps.add(String.format("%s, %s", cursor.getString(2), cursor.getString(1)));
            }
            tryCloseCursor(cursor);
            return nameDeps;
        }catch (Exception e) {
            Log.e(TAG, "getAllSotrName -> ".concat(e.getMessage()));
            return nameDeps;
        }finally {
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public Sotr getSotrReq(@NonNull int Id_s){

        Sotr sotr = new Sotr(Id_s, "0",0,0);
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT Id_o, Id_d, division_code FROM Sotr Where _id=?", new String [] {String.valueOf(Id_s)});
            if (cursor != null && cursor.moveToFirst()){
                sotr.set_Id_o(cursor.getInt(0));
                sotr.set_Id_d(cursor.getInt(1));
                sotr.setDivision_code(cursor.getString(2));
            }
            return sotr;
        }catch (Exception e) {
            Log.e(TAG, "getSotrReq -> ".concat(e.getMessage()));
            return sotr;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public int getOneSotrIdByDepId(int depId){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public int getSotr_id_by_Name(String nm){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public String getNameById(int iD){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getUpdateDate() {
        final String updateDate = StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                    ? AppController.getInstance().getGlobalUpdateDate() : getTodayMorning();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM sotr", null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) < sDateTimeToLong(updateDate)
                        ? cursor.getLong(0) : sDateTimeToLong(updateDate));
            }
            return SharedPrefs.getInstance().getInitUpdateDate();
        } catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return updateDate;
        } finally {
            tryCloseCursor(cursor);
        }
    }
}
