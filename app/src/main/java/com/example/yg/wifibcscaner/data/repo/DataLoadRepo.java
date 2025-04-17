package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
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
import com.example.yg.wifibcscaner.data.model.BoxSizing;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import org.apache.commons.collections4.CollectionUtils;
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

    private AtomicInteger nextPage = new AtomicInteger(0);
    private static int pageSize = 200;
    private static final String TAG = "sProject -> OutDocBoxMovePartRepository.";
    SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    /*
    public void loadStuff() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
                            //downloadDivision();
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
    }*/

    public void loadData() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
//                            loadStuff() ;

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
                    if (response.body() != null && (!response.body().orderReqList.isEmpty() || !response.body().outDocReqList.isEmpty()))
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
                                MessageUtils.showToast(AppController.getInstance().getString(R.string.data_load_in_progress), false);
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

            boolean outDocOk = false;
            if (CollectionUtils.isNotEmpty(r.outDocReqList)) {
                outDocOk = insertOutDocInBulk(r.outDocReqList);
            }

            if (CollectionUtils.isNotEmpty(r.orderReqList) && insertOrdersInBulk(r.orderReqList)) {

                if (outDocOk) {
                    if (!r.boxSizingReqList.isEmpty()) {
                        if (!insertBoxSizingInBulk(r.boxSizingReqList))
                            MessageUtils.showToast("Сервер не отвечает. Проверьте подключение WiFi.", true);
                    }

                    if (!r.boxReqList.isEmpty() && insertBoxInBulk(r.boxReqList)) {

                        if (!r.movesReqList.isEmpty() && insertBoxMoveInBulk(r.movesReqList)) {

                            if (!r.partBoxReqList.isEmpty()) insertProdInBulk(r.partBoxReqList);
                        }
                    }
                }
                if (mDataBase.inTransaction()) mDataBase.setTransactionSuccessful();
                return Collections.max(r.orderReqList, Comparator.comparing(Orders::get_DT)).get_DT();
            }
            if (mDataBase.inTransaction()) mDataBase.setTransactionSuccessful();
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
                Log.d(TAG, String.valueOf(statement.executeInsert()));
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
                final long id = statement.executeInsert();
                if (id == -1L) Log.i(TAG, "insertOutDocInBulk insert error on: "+ o.get_id());
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

    public boolean insertBoxSizingInBulk(List<BoxSizing> list) {
        try {
            SQLiteStatement statement = mDataBase.compileStatement(BoxSizing.SQL_INSERT_REPLACE);

            for (BoxSizing o : list) {
                statement.clearBindings();
                // statement.bindLong(1, o.getId());
                statement.bindLong(1, o.getMasterDataId());
                statement.bindLong(2, o.getQuantity());
                statement.bindString(3, o.getSize());
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
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM " + Orders.TABLE_NAME, null);
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


}
