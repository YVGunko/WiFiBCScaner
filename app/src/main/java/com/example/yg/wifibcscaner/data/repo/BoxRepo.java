package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.PartBoxRequest;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.service.MessageUtils.showToast;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class BoxRepo {
    private static final String TAG = "sProject -> BoxRepo.";

    public static String makeBoxNumber(@NonNull String num) {
        StringBuilder sb = new StringBuilder();
        sb.append("№ кор: ");
        sb.append(num);
        sb.append(" ");
        return sb.toString();
    }

    public static String makeBoxDesc(@NonNull String num, @NonNull String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("№ кор: ");
        sb.append(num);
        sb.append(", Принято: ");
        sb.append(q);
        return sb.toString();
    }

    public void insertBoxInBulk(List<Boxes> list){
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            mDataBase.beginTransaction();
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

            mDataBase.setTransactionSuccessful();
            // TODO to remove

            showToast("insertBoxInBulk. success", true);
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    /* send outDocs in background
     * */
    public void sendData(String updateDate) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                ArrayList<Boxes> boxesList = AppController.getInstance().getDbHelper().getBoxes();
                ArrayList<BoxMoves> boxMovesList = AppController.getInstance().getDbHelper().getBoxMoves();
                ArrayList<Prods> prodsList = AppController.getInstance().getDbHelper().getProds();
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).addBoxes(new PartBoxRequest(boxesList, boxMovesList, prodsList),
                        AppController.getInstance().getDefs().get_idUser(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<PartBoxRequest>() {
                    @Override
                    public void onResponse(Call<PartBoxRequest> call, Response<PartBoxRequest> response) {
                        if (response.isSuccessful()) {
                            updateWithResponse (response.body());
                        } else {
                            Log.e(TAG, "Box sending response wasn't successful");
                        }
                    }

                    @Override
                    public void onFailure(Call<PartBoxRequest> call, Throwable t) {
                        Log.e(TAG, t.getMessage());
                    }
                });
            }catch (Exception e) {
                Log.e(TAG, "sendData -> " + e.getMessage());
            }
        });
        return;
    }
    public void updateWithResponse(@NonNull PartBoxRequest body) {
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            mDataBase.beginTransaction();
            ContentValues values = new ContentValues();

            try {
                for (Boxes b : body.boxReqList) {
                    values.put(Boxes.COLUMN_sentToMasterDate, sDateTimeToLong(b.get_sentToMasterDate()));
                    values.put(Boxes.COLUMN_archive, b.isArchive());
                    mDataBase.update(Boxes.TABLE_boxes, values, Boxes.COLUMN_ID + "='" + b.get_id() + "'", null) ;
                }
            }catch (SQLiteException e) {
                Log.e(TAG, "updateWithResponse -> Boxes sentToMasterDate update exception -> ".concat(e.getMessage()));
                throw new RuntimeException("To catch into upper level.");
            }
            try {
                values.clear();
                for (BoxMoves bm : body.movesReqList) {
                    values.put(BoxMoves.COLUMN_sentToMasterDate, sDateTimeToLong(bm.get_sentToMasterDate()));
                    mDataBase.update(BoxMoves.TABLE_bm, values,BoxMoves.COLUMN_ID +"='"+bm.get_id()+"'",null);
                }
            }catch (SQLiteException e) {
                Log.e(TAG, "updateWithResponse -> BoxMoves sentToMasterDate update exception -> ".concat(e.getMessage()));
                throw new RuntimeException("To catch into upper level.");
            }
            try {
                values.clear();
                for (Prods pb : body.partBoxReqList) {
                    values.put(Prods.COLUMN_sentToMasterDate, sDateTimeToLong(pb.get_sentToMasterDate()));
                    mDataBase.update(Prods.TABLE_prods, values,Prods.COLUMN_ID +"='"+pb.get_id()+ "'",null);
                }
            }catch (SQLiteException e) {
                Log.e(TAG, "updateWithResponse -> Prods sentToMasterDate update exception -> ".concat(e.getMessage()));
                throw new RuntimeException("To catch into upper level.");
            }

            showToast("Коробки выгружены успешно.", true);
            mDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    public boolean updateBoxesSetArchiveTrue(String bId) {
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            ContentValues values = new ContentValues();
            values.clear();

            values.put(Boxes.COLUMN_archive, true);
            return (mDataBase.update(Boxes.TABLE_boxes, values,Boxes.COLUMN_ID +"='"+bId+"'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
}
