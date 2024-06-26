package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getTodayMorning;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class OperRepo {
    private static final String TAG = "sProject -> OperRepo";
    private SQLiteDatabase mDataBase ;

    public OperDownloadListenner listenner;
    public interface OperDownloadListenner {
        void onSuccess();

        void onFail(Throwable t);
    }
    public void setListenner(OperDownloadListenner listenner) {
        this.listenner = listenner;
    }
    private void downloadOperation() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getOperation(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                            ? AppController.getInstance().getGlobalUpdateDate()
                            : getUpdateDate(getTodayMorning()))
                    .enqueue(new Callback<List<Operation>>() {
                        @Override
                        public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                            if (response.isSuccessful() && !response.body().isEmpty())
                                insertOperationInBulk(response.body());
                        }

                        @Override
                        public void onFailure(Call<List<Operation>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                            if (listenner != null) listenner.onFail(t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }
    public void insertOperationInBulk(List<Operation> list) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO "+Operation.TABLE+" (_id, dt, opers, division_code) " +
                    " VALUES (?,?,?,?) ";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Operation o : list) {
                statement.clearBindings();
                statement.bindLong(1, o.get_id());
                statement.bindString(2, o.get_dt());
                statement.bindString(3, o.get_Opers());
                statement.bindString(4, o.getDivision_code());

                statement.executeInsert();
            }
            mDataBase.setTransactionSuccessful();
            if (listenner != null) listenner.onSuccess();
        } catch (Exception e) {
            Log.w(TAG, e);
            if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
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
    private String getUpdateDate(@NonNull String globalUpdateDate) {
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM operation", null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) > sDateTimeToLong(globalUpdateDate) ? cursor.getLong(0) : sDateTimeToLong(globalUpdateDate));
            }
            return globalUpdateDate;
        } catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        } finally {
            tryCloseCursor(cursor);
        }
    }
}
