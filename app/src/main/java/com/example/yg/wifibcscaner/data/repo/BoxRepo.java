package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.PartBoxRequest;
import com.example.yg.wifibcscaner.service.foundOrder;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.service.MessageUtils.showToast;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.retStringFollowingCRIfNotNull;

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

    public void lastBoxCheck(foundOrder fo){
        Cursor c = null;
        String query;
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            try {
                query = "SELECT count(b._id) as N_box FROM Boxes b, BoxMoves bm Where Id_m=" + fo.get_id()+" and b._id=bm.Id_b and bm.Id_o="+AppController.getInstance().getDefs().get_Id_o();
                c = mDataBase.rawQuery(query, null);
                if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                    c.moveToFirst(); //есть boxes & prods
                    if(fo.getNB() == c.getInt(0))
                        MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                                "Это последняя коробка этого размера из заказа!",
                                false);
                }
            } catch (SQLException e) {
                Log.e(TAG, "lastBoxCheck exception on id -> " + fo.get_id() , e);
            }
        }finally {
            tryCloseCursor(c);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    //list all boxes
    public ArrayList<HashMap<String, Integer>> listboxes() {
        ArrayList<HashMap<String, Integer>> readBoxes = new ArrayList<HashMap<String, Integer>>();
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            if (!AppUtils.isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o()))
                cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                        "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, MasterData.Ord_id, Boxes._id, bm._id, Prods._id" +
                        " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s Where Opers._id=" + AppController.getInstance().getDefs().get_Id_o() +
                        " and bm.Id_o=Opers._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and bm._id=Prods.Id_bm" +
                        " and Prods.Id_d=Deps._id and Prods.Id_s=s._id and ((Prods.sentToMasterDate IS NULL) OR (Prods.sentToMasterDate=''))" +
                        " Order by MasterData.Ord_id,  Boxes.N_box", null);
            else
                cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                        "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, MasterData.Ord_id, Boxes._id, bm._id, Prods._id" +
                        " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s Where Opers._id=" + AppController.getInstance().getDefs().get_Id_o() +
                        " and bm.Id_o=Opers._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and bm._id=Prods.Id_bm and Prods.Id_d=" + AppController.getInstance().getDefs().get_Id_d() +
                        " and Prods.Id_d=Deps._id and Prods.Id_s=s._id and ((Prods.sentToMasterDate IS NULL) OR (Prods.sentToMasterDate=''))" +
                        " Order by MasterData.Ord_id,  Boxes.N_box", null);

            while (cursor.moveToNext()) {
                HashMap readBox = new HashMap<String, Integer>();
                String sTmp = null;
                if (!AppUtils.isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o())) sTmp = "";
                else sTmp = cursor.getString(8) + ", " + cursor.getString(9);
                //Заполняем
                readBox.put("Ord", cursor.getString(0) + ". " + cursor.getString(1));
                readBox.put("Cust", "Подошва: " + cursor.getString(2) + ", " + retStringFollowingCRIfNotNull(cursor.getString(3))
                        + "Заказ: " + cursor.getString(4) + ". № кор: " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + " "
                        + "В кор: " + cursor.getString(7) + ". " + sTmp);
                readBox.put("bId", cursor.getString(11) + "/bId");
                readBox.put("bmId", cursor.getString(12) + "/bmId");
                readBox.put("pdId", cursor.getString(13) + "/pdId");
                //Закидываем в список
                readBoxes.add(readBox);
            }
            return readBoxes;
        }catch (Exception e) {
            Log.e(TAG, "getOutDocNotSent -> ".concat(e.getMessage()) );
            return readBoxes;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }

    /* send boxes in background
     * */
    public void sendData(String updateDate) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                ArrayList<Boxes> boxesList = AppController.getInstance().getDbHelper().getBoxes();
                ArrayList<BoxMoves> boxMovesList = AppController.getInstance().getDbHelper().getBoxMoves();
                ArrayList<Prods> prodsList = AppController.getInstance().getDbHelper().getProds();
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).partBox(new PartBoxRequest(boxesList, boxMovesList, prodsList),
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
                // TODO updateBoxesSetArchiveTrue
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
