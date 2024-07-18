package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.Result;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;

public class DivisionRepo {
    private static final String TAG = "sProject -> DivisionRepo";
    private SQLiteDatabase mDataBase ;

    public DivDownloadListenner listenner;
    public interface DivDownloadListenner {
        void onSuccess();

        void onFail(Throwable t);
    }
    public void setListenner(DivDownloadListenner listenner) {
        this.listenner = listenner;
    }
    public void downloadDivision() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                        .getDivision()
                        .enqueue(new Callback<List<Division>>() {
                            @Override
                            public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                                if (response.isSuccessful() && !response.body().isEmpty()) {
                                    insertDivisionInBulk(response.body());
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Division>> call, Throwable t) {
                                Log.d(TAG, "Ответ сервера на запрос Division: " + t.getMessage());
                                if (listenner != null) listenner.onFail(t);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "downloadUser -> ", e);
                if (listenner != null)
                    listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
            }
        });
        return;
    }
    public void insertDivisionInBulk(List<Division> list) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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

    public List<String> getAllDivisionName() {
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public String getDivisionNameByCode(@NonNull String code){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
}
