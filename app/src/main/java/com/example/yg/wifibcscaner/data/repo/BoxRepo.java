package com.example.yg.wifibcscaner.data.repo;

import android.database.sqlite.SQLiteDatabase;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;

public class BoxRepo {
    private static final String TAG = "sProject -> BoxRepo.";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

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

            mDataBase.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
            //mDataBase.execSQL("PRAGMA foreign_keys = 1;");
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
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
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<PartBoxRequest> call, Response<PartBoxRequest> response) {
                        if (response.isSuccessful()) {
                            for (Boxes boxReq : response.body().boxReqList)
                                if (!AppController.getInstance().getDbHelper().updateBoxesSentDate(boxReq))
                                    Log.e(TAG, "Ошибка при записи даты в Box.");
                            for (BoxMoves pmReq : response.body().movesReqList) {
                                if (!AppController.getInstance().getDbHelper().updateBoxMovesSentDate(pmReq))
                                    Log.e(TAG, "Ошибка при записи даты в BoxMoves.");
                                if (pmReq.get_Id_o() == AppController.getInstance().getDefs().get_idOperLast())
                                    if (!AppController.getInstance().getDbHelper().updateBoxesSetArchiveTrue(pmReq.get_Id_b()))
                                        Log.e(TAG, "Ошибка при установке признака архива Box.");
                            }
                            for (Prods pReq : response.body().partBoxReqList)
                                if (!AppController.getInstance().getDbHelper().updateProdsSentDate(pReq))
                                    Log.e(TAG, "Ошибка при записи даты в Prods.");
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
}
