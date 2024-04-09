package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.BuildConfig;
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

import static com.example.yg.wifibcscaner.DataBaseHelper.COLUMN_sentToMasterDate;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

public class DataSendRepo {
    private static final String TAG = "sProject -> DataSendRepo";
    SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
    /* send outDocs in background
     * */
    private boolean updateBoxesSetArchiveTrue(String bId) {
        //SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
    private void updateWithResponse(@NonNull PartBoxRequest body) {
        //SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
                    if (bm.get_Id_o() == AppController.getInstance().getDefs().get_idOperLast())
                        if (!updateBoxesSetArchiveTrue(bm.get_Id_b()))
                            Log.d("getBoxesService", "Ошибка при установке признака архива Box.");
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

            Log.d(TAG, "Коробки выгружены успешно!");
            mDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
        } finally {
            mDataBase.endTransaction();
            //AppController.getInstance().getDbHelper().closeDataBase();
            if (BuildConfig.DEBUG) MessageUtils.showToast("Коробки, мувы, парты. updateWithResponse SentToMasterDate успешно", true);
        }
    }
    private void updateOutDocsetSentToMasterDate (List<OutDocs> outDocs) {
        //SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            mDataBase.beginTransaction();
            ContentValues values = new ContentValues();
            try {
                for (OutDocs od : outDocs) {
                    values.put(COLUMN_sentToMasterDate, new Date().getTime());
                    mDataBase.update(OutDocs.TABLE, values, OutDocs.COLUMN_Id + "='" + od.get_id() + "'", null);
                }
            }catch (SQLiteException e) {
                Log.e(TAG, "updateOutDocsetSentToMasterDate -> update exception -> ".concat(e.getMessage()));
                throw new RuntimeException("To catch into upper level.");
            }
            mDataBase.setTransactionSuccessful();
            if (BuildConfig.DEBUG) MessageUtils.showToast("Накладные. Транзакция изменения статуса SentToMasterDate успешно", true);
        } catch (Exception e) {
            Log.e( TAG, "updateOutDocsetSentToMasterDate exception ".concat(e.getMessage()) );
        } finally {
            mDataBase.endTransaction();
            //AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    private ArrayList<OutDocs> getOutDocNotSent(){
        Cursor cursor = null;
        ArrayList<OutDocs> readBoxMoves = new ArrayList<OutDocs>();
        try {
            cursor = mDataBase.rawQuery("SELECT _id, Id_o, number, comment, DT, division_code, idUser, idSotr, idDeps" +
                    " FROM OutDocs where ((" + COLUMN_sentToMasterDate + " IS NULL) OR (" + COLUMN_sentToMasterDate + " = ''))", null);
            while (cursor.moveToNext()) {
                OutDocs readBoxMove = new OutDocs(cursor.getString(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        lDateToString(cursor.getLong(4)),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8));
                //Закидываем в список
                readBoxMoves.add(readBoxMove);
            }
            Log.d(TAG, "getOutDocNotSent -> ".concat(String.valueOf(readBoxMoves.size())) );
            return readBoxMoves;
        }catch (Exception e) {
            Log.e(TAG, "getOutDocNotSent -> ".concat(e.getMessage()) );
            return readBoxMoves;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    private void uploadData() {
        try {
            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).
                    addOutDoc(getOutDocNotSent(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<List<OutDocs>>() {
                @Override
                public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                    if(response.isSuccessful()) {
                        updateOutDocsetSentToMasterDate(response.body());

                        if (response.body().size()!=0) {
                            MessageUtils.showToast( "Ок! Накладные выгружены!", false);
                        }
                        try {
                            ArrayList<Boxes> boxesList = AppController.getInstance().getDbHelper().getBoxes();
                            ArrayList<BoxMoves> boxMovesList = AppController.getInstance().getDbHelper().getBoxMoves();
                            ArrayList<Prods> prodsList = AppController.getInstance().getDbHelper().getProds();
                            ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).partBox(new PartBoxRequest(boxesList, boxMovesList, prodsList),
                                    AppController.getInstance().getDefs().get_idUser(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<PartBoxRequest>() {
                                @Override
                                public void onResponse(Call<PartBoxRequest> call, Response<PartBoxRequest> response) {
                                    if (response.isSuccessful()) {
                                        updateWithResponse(response.body());
                                    } else {
                                        MessageUtils.showToast("Ошибка при выгрузке данных на сервер!", true);
                                    }
                                }

                                @Override
                                public void onFailure(Call<PartBoxRequest> call, Throwable t) {
                                    Log.d(TAG, t.getMessage());
                                    MessageUtils.showToast("Ошибка. ТаймАут.", true);
                                }
                            });
                        }catch (Exception e) {
                            MessageUtils.showToast("Ошибка при выгрузке коробок.", true);
                        }
                    }else {
                        MessageUtils.showToast("Ошибка при выгрузке накладных!", true);
                    }
                }
                @Override
                public void onFailure(Call<List<OutDocs>> call, Throwable t) {
                    MessageUtils.showToast("Ошибка при отправке данных!", true);
                    Log.e(TAG, "sendData onFailure: " + t.getMessage());
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "uploadData -> " , e);
            MessageUtils.showToast("Исключительная ситуация при выгрузке.", true);
        }
        return;
    }

    /* send boxes in background
     * */
    public void sendData() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                //check if connection is available
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful())
                            uploadData();
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.e(TAG, "onFailure при запросе времени обновления с сервера: " + t.getMessage());
                        MessageUtils.showToast("Ошибка при отправке данных!", true);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception запроса времени обновления с сервера : " + e.getMessage());
                MessageUtils.showToast("Исключительная ситуация при запросе времени обновления с сервера.", true);
                return ;
            }
        });
        return;
    }
}
