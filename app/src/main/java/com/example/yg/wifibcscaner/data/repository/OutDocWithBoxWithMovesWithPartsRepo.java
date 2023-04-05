package com.example.yg.wifibcscaner.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OutDocWithBoxWithMovesWithPartsIdOnlyRequest;
import com.example.yg.wifibcscaner.data.dto.OutDocWithBoxWithMovesWithPartsRequest;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.NotificationUtils;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;
import com.example.yg.wifibcscaner.utils.StringUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.DataBaseHelper.COLUMN_sentToMasterDate;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getLongDateTimeString;
import static com.example.yg.wifibcscaner.utils.DbUtils.tryCloseCursor;

public class OutDocWithBoxWithMovesWithPartsRepo {
    private static final String TAG = "outDocBoxMovesPartsRepo";
    private static final String MY_CHANNEL_ID = "Data Exchange";

    NotificationUtils notificationUtils;

    public void setNotificationUtils(NotificationUtils notificationUtils) {
        this.notificationUtils = notificationUtils;
    }
    /**
     * run an exchange sequence
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void call() {

        notificationUtils = new NotificationUtils();
        setNotificationUtils(notificationUtils);

        exchangeData();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void exchangeData() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                OutDocWithBoxWithMovesWithPartsRequest request = dataToSend();
                if (request.getPartBoxReqList().isEmpty()) {
                    Log.d(TAG, "exchangeData -> " + AppController.getInstance().getResourses().getString(R.string.no_data_to_exchange));
                    return;
                }
                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getOutDocPost(request).enqueue(new Callback<OutDocWithBoxWithMovesWithPartsIdOnlyRequest>() {
                    @Override
                    public void onResponse(Call<OutDocWithBoxWithMovesWithPartsIdOnlyRequest> call, Response<OutDocWithBoxWithMovesWithPartsIdOnlyRequest> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null & !response.body().getOutDocIdList().isEmpty()) {
                                applyResponce(response.body(), getDayTimeLong(new Date()));
                                Log.d(TAG, "exchangeData -> " + AppController.getInstance().getResourses().getString(R.string.downloaded_succesfully));
                                if (notificationUtils != null)
                                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.downloaded_succesfully), MY_CHANNEL_ID);
                                    });
                            }else {
                                Log.e(TAG, "exchangeData -> " + AppController.getInstance().getResourses().getString(R.string.data_exchage_went_wrong));
                                if (notificationUtils != null)
                                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.data_exchage_went_wrong), MY_CHANNEL_ID);
                                    });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<OutDocWithBoxWithMovesWithPartsIdOnlyRequest> call, Throwable t) {
                        Log.w(TAG, "exchangeData -> " + t.getMessage());
                        if (notificationUtils != null)
                            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                                notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.data_exchage_went_wrong), MY_CHANNEL_ID);
                            });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "exchangeData -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private OutDocWithBoxWithMovesWithPartsRequest dataToSend() {
        OutDocWithBoxWithMovesWithPartsRequest responce = new OutDocWithBoxWithMovesWithPartsRequest();
        SQLiteDatabase db = AppController.getInstance().getDbHelper().getDb();
        boolean dbWasOpen = false;
        Cursor cursor = null;
        try {
            if (!db.isOpen()) {
                db = AppController.getInstance().getDbHelper().getReadableDatabase();
            } else dbWasOpen = true;

            responce.partBoxReqList.addAll(
                    getPartBoxNotSent(cursor, db));

            responce.outDocReqList.addAll(getOutDoc(cursor, db,
                    StringUtils.toSqlInString(responce.partBoxReqList.stream()
                            .map(Prods::get_idOutDocs)
                            .distinct()
                            .collect(Collectors.toList()))));

            responce.movesReqList.addAll(
                    getBoxMoves(cursor, db,
                            StringUtils.toSqlInString(responce.partBoxReqList.stream()
                                    .map(Prods::get_Id_bm)
                                    .distinct()
                                    .collect(Collectors.toList()))));
            responce.boxReqList.addAll(
                    getBoxes(cursor, db,
                            StringUtils.toSqlInString(responce.movesReqList.stream()
                                    .map(BoxMoves::get_Id_b)
                                    .distinct()
                                    .collect(Collectors.toList()))));

        }finally {
            tryCloseCursor(cursor);
            if (!dbWasOpen) db.close();
            return responce;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Boxes> getBoxes(Cursor cursor, SQLiteDatabase db, String collect) {
        ArrayList<Boxes> responce = new ArrayList<>();
        try {
            String sql = "SELECT _id,Id_m,Q_box,N_box,DT FROM Boxes where "
                    +Boxes.COLUMN_ID+" in ("+collect+")";
            cursor = db.rawQuery(sql, null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Boxes readBox = new Boxes(cursor.getString(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            getLongDateTimeString((cursor.getLong(4))),
                            null,
                            false);
                    if ((readBox.get_id()!= "")&(((int) readBox.get_Id_m()) != 0))
                        responce.add(readBox);
                    cursor.moveToNext();
                }
            }
        }finally {
            tryCloseCursor(cursor);
            return responce;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<BoxMoves> getBoxMoves(Cursor cursor, SQLiteDatabase db, String collect) {
        ArrayList<BoxMoves> responce = new ArrayList<>();
        try {
            String sql = "SELECT bm._id,bm.Id_b,bm.Id_o,bm.DT FROM BoxMoves bm where "
                    +BoxMoves.COLUMN_ID+" in ("+collect+")";

            cursor = db.rawQuery(sql, null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    BoxMoves readBoxMove = new BoxMoves(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getInt(2),
                            getLongDateTimeString((cursor.getLong(3))),
                            null);
                    responce.add(readBoxMove);
                    cursor.moveToNext();
                }
            }

        }finally {
            tryCloseCursor(cursor);
            return responce;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Prods> getPartBoxNotSent(Cursor cursor, SQLiteDatabase db) {
        ArrayList<Prods> responce = new ArrayList<>();
        try {
            String sql = "SELECT _id, Id_bm,Id_d,Id_s,RQ_box,P_date,sentToMasterDate,idOutDocs FROM Prods where (("
                    + COLUMN_sentToMasterDate + " IS NULL) OR (" + COLUMN_sentToMasterDate + " = ''))";

            cursor = db.rawQuery(sql, null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Prods readProd = new Prods(cursor.getString(0),
                            cursor.getString(1), cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getInt(4),
                            getLongDateTimeString(cursor.getLong(5)),
                            null,
                            cursor.getString(7));
                    responce.add(readProd);
                    cursor.moveToNext();
                }
            }

        }finally {
            tryCloseCursor(cursor);
            return responce;
        }
    }


    /* get OutDoc */
    private ArrayList<OutDocs> getOutDoc(Cursor cursor, SQLiteDatabase db, String collect){
        ArrayList<OutDocs> responce = new ArrayList<>();
        try {
            String sql = "SELECT _id, number, comment, DT, Id_o, division_code, idUser" +
                    " FROM OutDocs where _id <> '0' and "
                    +OutDocs.COLUMN_Id+" in ("+collect+")";

            cursor = db.rawQuery(sql, null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();

                //Пробегаем по всем коробкам
                while (!cursor.isAfterLast()) {
                    OutDocs readBoxMove = new OutDocs(cursor.getString(0),
                            cursor.getInt(4),
                            cursor.getInt(1),
                            cursor.getString(2),
                            getLongDateTimeString(cursor.getLong(3)),
                            null,
                            cursor.getString(5),
                            cursor.getInt(6));
                    //Закидываем в список
                    responce.add(readBoxMove);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        }finally {
            tryCloseCursor(cursor);
            return responce;
        }
    }
    // Test purpose only
    private ArrayList<OutDocs> getOutDocTest(Cursor cursor, SQLiteDatabase db){
        ArrayList<OutDocs> responce = new ArrayList<>();
        try {
            String sql = "SELECT _id, number, comment, DT, Id_o, division_code, idUser" +
                    " FROM OutDocs where _id <> '0' and Id_o = 9999 LIMIT 5";

            cursor = db.rawQuery(sql, null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();

                //Пробегаем по всем коробкам
                while (!cursor.isAfterLast()) {
                    OutDocs readBoxMove = new OutDocs(cursor.getString(0), cursor.getInt(4), cursor.getInt(1), cursor.getString(2),
                            getLongDateTimeString(cursor.getLong(3)), null, cursor.getString(5), cursor.getInt(6));
                    //Закидываем в список
                    responce.add(readBoxMove);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        }finally {
            tryCloseCursor(cursor);
            return responce;
        }
    }


    private void applyResponce(OutDocWithBoxWithMovesWithPartsIdOnlyRequest body, Long dateToSet) {
        SQLiteDatabase db = AppController.getInstance().getDbHelper().getDb();
        boolean dbWasOpen = false;
        Cursor cursor = null;
        try {
            if (!db.isOpen() || db.isReadOnly()) {
                db = AppController.getInstance().getDbHelper().getWritableDatabase();
            } else dbWasOpen = true;
            try {
                db.beginTransaction();
                String sql = "UPDATE OutDocs SET sentToMasterDate = ? " +
                        " WHERE _id = ? ";

                SQLiteStatement statement = db.compileStatement(sql);

                for (String o : body.getOutDocIdList()) {
                    statement.clearBindings();
                    statement.bindLong(1, dateToSet);
                    statement.bindString(2, o);
                    if (statement.executeUpdateDelete() == 0)
                        Log.e(TAG, "applyResponce -> update OutDocs -> didn't save "+o);
                }

                sql = "UPDATE Boxes SET sentToMasterDate = ? " +
                        " WHERE _id = ? ";

                statement = db.compileStatement(sql);

                for (String o : body.getBoxIdList()) {
                    statement.clearBindings();
                    statement.bindLong(1, dateToSet);
                    statement.bindString(2, o);
                    if (statement.executeUpdateDelete() == 0)
                        Log.e(TAG, "applyResponce -> update Boxes -> didn't save "+o);
                }

                sql = "UPDATE BoxMoves SET sentToMasterDate = ? " +
                        " WHERE _id = ? ";

                statement = db.compileStatement(sql);

                for (String o : body.getBoxMoveIdList()) {
                    statement.clearBindings();
                    statement.bindLong(1, dateToSet);
                    statement.bindString(2, o);
                    if (statement.executeUpdateDelete() == 0)
                        Log.e(TAG, "applyResponce -> update BoxMoves -> didn't save "+o);
                }

                sql = "UPDATE Prods SET sentToMasterDate = ? " +
                        " WHERE _id = ? ";

                statement = db.compileStatement(sql);

                for (String o : body.getPartBoxIdList()) {
                    statement.clearBindings();
                    statement.bindLong(1, dateToSet);
                    statement.bindString(2, o);
                    if (statement.executeUpdateDelete() == 0)
                        Log.e(TAG, "applyResponce -> update Prods -> didn't save "+o);
                }

                db.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
                //db.execSQL("PRAGMA foreign_keys = 1;");
            } catch (Exception e) {
                Log.w(TAG, e);
                throw new RuntimeException("To catch into upper level.");
            } finally {
                db.endTransaction();
            }
        }finally {
            tryCloseCursor(cursor);
            if (!dbWasOpen) db.close();
        }
    }
    public boolean updateOutDocsetSentToMasterDate (SQLiteDatabase mDataBase, OutDocs od) {
        boolean b = false;
        try {
            try {
                //mDataBase = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.clear();

                values.put(COLUMN_sentToMasterDate, new Date().getTime());
                b = (mDataBase.update(OutDocs.TABLE, values,OutDocs.COLUMN_Id +"='"+od.get_id()+"'",null) > 0) ;
            } catch (SQLiteException e) {
                // TODO: handle exception
                return false;
            }
        }finally {
            mDataBase.close();
            return b;
        }
    }
}
