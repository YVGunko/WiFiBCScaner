package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

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
    public DivListenner listenner;
    public interface DivListenner {
        void onSuccess(String message);

        void onFail(Throwable t);
    }
    public void setListenner(DivListenner listenner) {
        this.listenner = listenner;
    }
    public void callApi() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
                            if (listenner != null) listenner.onSuccess("response.body()");
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.e(TAG, "onFailure при запросе времени обновления с сервера: " + t.getMessage());
                        MessageUtils.showToast("Ошибка при синхронизации данных!", true);
                        if (listenner != null) listenner.onFail(t);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception запроса времени обновления с сервера : " + e.getMessage());
                MessageUtils.showToast("Исключительная ситуация при запросе времени обновления с сервера.", true);
                if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
                return;
            }
        });
        return;
    }
    private static final String TAG = "sProject -> DivisionRepo";
    private SQLiteDatabase mDataBase ;

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
