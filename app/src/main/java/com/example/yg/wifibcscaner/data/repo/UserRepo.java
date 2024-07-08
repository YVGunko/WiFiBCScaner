package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getTodayMorning;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class UserRepo {
    private static final String TAG = "sProject -> UserRepo";
    private SQLiteDatabase mDataBase ;

    public UserDownLoadListenner listenner;
    public interface UserDownLoadListenner {
        void onSuccess();

        void onFail(Throwable t);
    }
    public void setListenner(UserDownLoadListenner listenner) {
        this.listenner = listenner;
    }
    public void downloadUser() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getUser()
                    .enqueue(new Callback<List<user>>() {
                        @Override
                        public void onResponse(Call<List<user>> call, Response<List<user>> response) {
                            if (response.isSuccessful() && !response.body().isEmpty())
                                insertUser(response.body());
                        }

                        @Override
                        public void onFailure(Call<List<user>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                            if (listenner != null) listenner.onFail(t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
        }
        return;
    }

    private String getUserUpdateDate(@NonNull String globalUpdateDate) {
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM user", null);
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
    public boolean checkSuperUser (int _id) {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public String getUserName(int code){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    public int getUserSotrById(int _id){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public List<String> getAllUserName() {
        ArrayList<String> alUserName = new ArrayList<String>();
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public boolean checkIfUserTableEmpty () {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    /* Insert data */
    private void insertUserInBulk(List<user> user) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            mDataBase.beginTransaction();

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
    public long insertUser(List<user> list) {
        long counter = 0L;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            for (user user : list) {
                values.clear();
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_id, user.get_id());
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_Id_s, user.get_Id_s());
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_name, user.getName());
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_pswd, user.getPswd());
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_DT, sDateTimeToLong(user.get_DT()));
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_superUser, user.isSuperUser());
                values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_EXPIRED, user.isExpired());

                counter += mDataBase.insertWithOnConflict(com.example.yg.wifibcscaner.data.model.user.TABLE, null, values, 5);
            }
            if (listenner != null) listenner.onSuccess();
            return counter;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            if (listenner != null) listenner.onFail(e.getCause() != null ? e.getCause() : e.fillInStackTrace());
            return 0;
        } finally {
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    private long insertUser(user user) {
        try {
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
