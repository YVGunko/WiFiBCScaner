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

import com.example.yg.wifibcscaner.BuildConfig;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.Result;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getTodayMorning;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class DataLoadRepo {
    public interface RepositoryCallback<T> {
        void onComplete(Result<T> result);
    }

    private AtomicInteger nextPage = new AtomicInteger(0);
    private static int pageSize = 200;
    private static final String TAG = "sProject -> OutDocBoxMovePartRepository.";
    SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    //
    public void loadStuff(RepositoryCallback<String> repositoryCallback) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
                            downloadDivision();
                            downloadOperation();
                            downloadUser();
                            downloadSotr();
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.e(TAG, "onFailure при запросе времени обновления с сервера: " + t.getMessage());
                        MessageUtils.showToast("Ошибка при синхронизации данных!", true);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception запроса времени обновления с сервера : " + e.getMessage());
                MessageUtils.showToast("Исключительная ситуация при запросе времени обновления с сервера.", true);
                return;
            }
        });
        return;
    }

    private void downloadDivision() {
    }

    public void loadData() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
                            loadStuff(new RepositoryCallback<String>() {
                                @Override
                                public void onComplete(Result<String> result) {
                                    if (result instanceof Result.Success) {                     // Happy path                 } else {                     // Show error in UI                 }             }         }
                            );
                            if (BuildConfig.DEBUG) {
                                MessageUtils.showToast("Update date: "
                                        .concat(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                                                ? AppController.getInstance().getGlobalUpdateDate()
                                                : getOrderUpdateDate(getTodayMorning())), true);
                            }
                            downloadData(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                                    ? AppController.getInstance().getGlobalUpdateDate()
                                    : getOrderUpdateDate(getTodayMorning()));
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.e(TAG, "onFailure при запросе времени обновления с сервера: " + t.getMessage());
                        MessageUtils.showToast("Ошибка при синхронизации данных!", true);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception запроса времени обновления с сервера : " + e.getMessage());
                MessageUtils.showToast("Исключительная ситуация при запросе времени обновления с сервера.", true);
                return;
            }
        });
        return;
    }

    private void downloadUser() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getUser(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                            ? AppController.getInstance().getGlobalUpdateDate()
                            : getUserUpdateDate(getTodayMorning()))
                    .enqueue(new Callback<List<user>>() {
                        @Override
                        public void onResponse(Call<List<user>> call, Response<List<user>> response) {
                            if (response.isSuccessful() && !response.body().isEmpty()) {
                                for (user user : response.body())
                                    insertUser(user);
                                MessageUtils.showToast(String.valueOf(R.string.data_load_in_progress), true);
                                final RepositoryCallback<String> callback = new RepositoryCallback() {
                                    @Override
                                    public void onComplete(Result result) {
                                        if (result instanceof Result.Success) {
                                            // Happy path
                                        } else {
                                            // Show error in UI
                                        }
                                    }
                                };
                                callback.onComplete(result);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<user>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }
    private void downloadDivision() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getDivision()
                    .enqueue(new Callback<List<Division>>() {
                        @Override
                        public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                            if (response.isSuccessful() && !response.body().isEmpty()) {
                                insertDivisionInBulk(response.body());
                                MessageUtils.showToast(AppController.getInstance().getContext().getResources().getString(R.string.data_load_in_progress), true);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Division>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }
    private void downloadOperation() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getOperation(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                            ? AppController.getInstance().getGlobalUpdateDate()
                            : getUserUpdateDate(getTodayMorning()))
                    .enqueue(new Callback<List<Operation>>() {
                        @Override
                        public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                            if (response.isSuccessful() && response.code() == 200) {
                                insertOperationInBulk(response.body());
                                MessageUtils.showToast(String.valueOf(R.string.data_load_in_progress), true);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Operation>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }
    private void downloadSotr() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl())
                    .getSotr(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                            ? AppController.getInstance().getGlobalUpdateDate()
                            : getUserUpdateDate(getTodayMorning()))
                    .enqueue(new Callback<List<Sotr>>() {
                        @Override
                        public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {
                            if (response.isSuccessful() && response.code() == 200) {
                                insertSotrInBulk(response.body());
                                MessageUtils.showToast(String.valueOf(R.string.data_load_in_progress), true);
                            }
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
                            } catch (Exception e) {
                                Log.w(TAG, e);
                                throw new RuntimeException("To catch into upper level.");
                            } finally {
                                mDataBase.endTransaction();
                                AppController.getInstance().getDbHelper().closeDataBase();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Sotr>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "downloadUser -> ", e);
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }
    private void downloadData(String updateDate) {
        try {
            nextPage.set(0);

            Log.d(TAG, "downloadData -> update date: " + updateDate);
            Log.d(TAG, "downloadData -> current page to load: " + nextPage);

            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getDataPageableV1(
                    updateDate,
                    AppController.getInstance().getDefs().getDivision_code(),
                    AppController.getInstance().getDefs().get_Id_o(),
                    nextPage.getAndIncrement(),
                    pageSize)
                    .enqueue(downloadDataCallback(updateDate));

        } catch (Exception e) {
            Log.e(TAG, "downloadData -> ", e);
            MessageUtils.showToast("Ошибка. Загрузка данных. ", true);
        }
        return;
    }

    private Callback<OrderOutDocBoxMovePart> downloadDataCallback(String updateDate) {
        return new Callback<OrderOutDocBoxMovePart>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<OrderOutDocBoxMovePart> call, Response<OrderOutDocBoxMovePart> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Responce code: " + response.code());
                    if (response.code() == 204) {
                        //no content, so prepare environment to stop current request and prepare for next one
                        nextPage.set(0);
                        AppController.getInstance().setGlobalUpdateDate(getTodayMorning());
                        MessageUtils.showToast("Синхронизация завершена успешно.", true);
                        return;
                    }
                    if (response.code() != 200) return;
                    //save order, boxes, boxMoves, partBox
                    if (response.body() != null &&
                            response.body().orderReqList != null &&
                            !response.body().orderReqList.isEmpty())
                        try {
                            Log.d(TAG, "saveToDB here.");
                            String dt = saveToDB(response.body());
                            if (StringUtils.isEmpty(dt)) return;

                            Log.d(TAG, "downloadDataCallback -> pageNumber: " + nextPage.get());

                            if (nextPage.get() != 0) {
                                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getDataPageableV1(
                                        updateDate,
                                        AppController.getInstance().getDefs().getDivision_code(),
                                        AppController.getInstance().getDefs().get_Id_o(),
                                        nextPage.getAndIncrement(),
                                        pageSize)
                                        .enqueue(downloadDataCallback(updateDate));
                                MessageUtils.showToast(String.valueOf(R.string.data_load_in_progress), false);
                                if (BuildConfig.DEBUG) MessageUtils.showToast("Page ".concat(nextPage.toString()).concat(" has been requested."), true);
                            }
                        } catch (RuntimeException re) {
                            Log.w(TAG, re);
                            nextPage.set(0);
                        }
                }
            }

            @Override
            public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                Log.w(TAG, "downloadDataCallback -> API Request failed: " + t.getMessage());
                nextPage.set(0);
                MessageUtils.showToast("Сервер не отвечает. Проверьте подключение WiFi.", true);
            }

        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String saveToDB(OrderOutDocBoxMovePart r) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            mDataBase.beginTransaction();
            if (insertOrdersInBulk(r.orderReqList)) {

                if (r.outDocReqList != null &&
                        !r.outDocReqList.isEmpty() &&
                        insertOutDocInBulk(r.outDocReqList)) {

                    if (r.boxReqList != null &&
                            !r.boxReqList.isEmpty() &&
                            insertBoxInBulk(r.boxReqList)) {

                        if (r.movesReqList != null &&
                                !r.movesReqList.isEmpty() &&
                                insertBoxMoveInBulk(r.movesReqList)) {

                            if (r.partBoxReqList != null &&
                                    !r.partBoxReqList.isEmpty() &&
                                    insertProdInBulk(r.partBoxReqList)) {

                                mDataBase.setTransactionSuccessful();
                                return Collections.max(r.orderReqList, Comparator.comparing(Orders::get_DT)).get_DT();
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException re) {
            Log.w(TAG, re);
            throw new RuntimeException("To catch onto method level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
            Log.d(TAG, "saveToDB reached its return point.");
        }
        return "";
    }

    public boolean insertOrdersInBulk(List<Orders> list) {
        try {
            String sql = "INSERT OR REPLACE INTO MasterData (_id, Ord_id, Ord, Cust, Nomen, Attrib," +
                    " Q_ord, Q_box, N_box, DT, archive, division_code)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Orders o : list) {
                statement.clearBindings();
                statement.bindLong(1, o.get_id());
                statement.bindString(2, o.get_Ord_Id());
                statement.bindString(3, o.get_Ord());
                statement.bindString(4, o.get_Cust());
                statement.bindString(5, o.get_Nomen());
                if (o.get_Attrib() == null)
                    statement.bindString(6, "");
                else
                    statement.bindString(6, (o.get_Attrib()));
                statement.bindLong(7, o.get_Q_ord());
                statement.bindLong(8, o.get_Q_box());
                statement.bindLong(9, o.get_N_box());
                statement.bindLong(10, getDateTimeLong(o.get_DT()));
                if (o.getArchive() == null)
                    statement.bindLong(11, 0);
                else
                    statement.bindLong(11, (o.getArchive() ? 1 : 0));
                statement.bindString(12, o.getDivision_code());
                statement.executeInsert();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        }
    }

    public boolean insertOutDocInBulk(List<OutDocs> list) {
        try {
            String sql = "INSERT OR REPLACE INTO OutDocs (_id, Id_o, number, comment, DT, sentToMasterDate, division_code, idUser) " +
                    " VALUES (?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (OutDocs o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindLong(2, o.get_Id_o());
                statement.bindLong(3, o.get_number());
                if (o.get_comment() == null)
                    statement.bindString(4, "");
                else
                    statement.bindString(4, o.get_comment());

                statement.bindLong(5, getDateTimeLong(o.get_DT()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(6, new Date().getTime());
                else
                    statement.bindLong(6, getDateTimeLong(o.get_sentToMasterDate()));

                statement.bindString(7, o.getDivision_code());
                statement.bindLong(8, o.getIdUser());
                statement.executeInsert();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        }
    }

    public boolean insertBoxInBulk(List<Boxes> list) {
        try {
            String sql = "INSERT OR REPLACE INTO Boxes (_id, Id_m, Q_box, N_box, DT, sentToMasterDate, archive) " +
                    " VALUES (?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Boxes o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindLong(2, o.get_Id_m());
                statement.bindLong(3, o.get_Q_box());
                statement.bindLong(4, o.get_N_box());
                statement.bindLong(5, getDateTimeLong(o.get_DT()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(6, new Date().getTime());
                else
                    statement.bindLong(6, getDateTimeLong(o.get_sentToMasterDate()));

                statement.bindLong(7, (o.isArchive() ? 1 : 0));
                statement.executeInsert();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        }
    }

    public boolean insertBoxMoveInBulk(List<BoxMoves> list) {
        try {
            String sql = "INSERT OR REPLACE INTO BoxMoves (_id, Id_b, Id_o, DT, sentToMasterDate) " +
                    " VALUES (?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (BoxMoves o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindString(2, o.get_Id_b());
                statement.bindLong(3, o.get_Id_o());
                statement.bindLong(5, getDateTimeLong(o.get_DT()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(4, new Date().getTime());
                else
                    statement.bindLong(4, getDateTimeLong(o.get_sentToMasterDate()));

                statement.executeInsert();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        }
    }

    public boolean insertProdInBulk(List<Prods> list) {
        // SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            String sql = "INSERT OR REPLACE INTO Prods (_id, Id_bm, Id_d, Id_s, RQ_box, P_date, sentToMasterDate, idOutDocs) " +
                    " VALUES (?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Prods o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindString(2, o.get_Id_bm());
                statement.bindLong(3, o.get_Id_d());
                statement.bindLong(4, o.get_Id_s());
                statement.bindLong(5, o.get_RQ_box());
                statement.bindLong(6, getDateLong(o.get_P_date()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(7, new Date().getTime());
                else
                    statement.bindLong(7, getDateTimeLong(o.get_sentToMasterDate()));
                statement.bindString(8, o.get_idOutDocs());
                statement.executeInsert();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        }
    }

    private String getOrderUpdateDate(@NonNull String globalUpdateDate) {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM " + Orders.TABLE_orders, null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) > sDateTimeToLong(globalUpdateDate) ? cursor.getLong(0) : sDateTimeToLong(globalUpdateDate));
            }
            return globalUpdateDate;
        } catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
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
    /* Insert data */
    private void insertUserInBulk(List<user> user) {
        try {
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
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
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
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
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
}
