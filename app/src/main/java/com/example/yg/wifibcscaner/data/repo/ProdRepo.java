package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getUUID;

public class ProdRepo {
    private static final String TAG = "sProject -> ProdRepo.";

    public boolean addProds(foundBox fb) {
        try {
            BoxMoves bm = new BoxMoves (getUUID(),fb.get_id(), AppController.getInstance().getDefs().get_Id_o(),lDateToString(new Date().getTime()),null);
            if (insertBoxMoves(bm)) {
                Prods prod ;
                if (AppUtils.isDepAndSotrOper(bm.get_Id_o())) {// it needs Dep and Sotr
                    prod = new Prods(getUUID(),
                            bm.get_id(),
                            AppController.getInstance().getDefs().get_Id_d(),
                            AppController.getInstance().getDefs().get_Id_s(),
                            fb.getRQ(),
                            DateTimeUtils.getStartOfDayString(new Date()),
                            null,
                            AppController.getInstance().getCurrentOutDoc().get_id());
                }
                else {
                    prod = new Prods(getUUID(),
                            bm.get_id(),
                            0,
                            0,
                            fb.getRQ(),
                            DateTimeUtils.getStartOfDayString(new Date()),
                            null,
                            AppController.getInstance().getCurrentOutDoc().get_id());
                }
                return (insertOneProd(prod) > 0);
            } else return false;
        } catch (Exception e) {
            Log.e(TAG, "insertOneProd exception -> ".concat(e.getMessage()));
            return false;
        }
    }
    public void insertProdInBulk(List<Prods> list) {
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            mDataBase.beginTransaction();
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

            mDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public String getProdsMinAndMaxDate(){
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT min(P_date), max (P_date) FROM Prods", null);
            if (cursor != null && cursor.moveToFirst()){
                return lDateToString(cursor.getLong(0)).concat(" - ").concat(lDateToString(cursor.getLong(1)));
            }
            return DateTimeUtils.getDayTimeString(new Date());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return DateTimeUtils.getDayTimeString(new Date());
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
}
