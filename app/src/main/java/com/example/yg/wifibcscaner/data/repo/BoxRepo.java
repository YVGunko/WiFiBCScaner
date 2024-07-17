package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.service.foundOrder;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static android.database.Cursor.FIELD_TYPE_NULL;
import static com.example.yg.wifibcscaner.data.model.Prods.COLUMN_Id_d;
import static com.example.yg.wifibcscaner.data.model.Prods.COLUMN_idOutDocs;
import static com.example.yg.wifibcscaner.data.model.Prods.COLUMN_sentToMasterDate;
import static com.example.yg.wifibcscaner.data.model.Prods.TABLE_prods;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.retStringFollowingCRIfNotNull;

public class BoxRepo {
    private static final String TAG = "sProject -> BoxRepo";

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
    public ArrayList<HashMap<String, Integer>> listboxes(String outDocId, int depId, boolean sentToMasterDate) {
        ArrayList<HashMap<String, Integer>> readBoxes = new ArrayList<HashMap<String, Integer>>();
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            String addWhereOutDoc = "";
            if (StringUtils.isNotBlank(outDocId))
                addWhereOutDoc = addWhereOutDoc.concat(" and ").concat(TABLE_prods).concat(".").concat(COLUMN_idOutDocs).concat("='").concat(outDocId).concat("'");
            String addWhereDepartment = "";
            if (depId != 0)
                addWhereDepartment = addWhereDepartment.concat(" and ")
                        .concat(TABLE_prods).concat(".").concat(COLUMN_Id_d).concat("=")
                        .concat(String.valueOf(depId));
            String addWhereSentToMasterDate = "";
            if (sentToMasterDate)
                addWhereSentToMasterDate = addWhereSentToMasterDate.concat(" and ")
                        .concat(TABLE_prods).concat(".").concat(COLUMN_sentToMasterDate).concat(" IS NULL ");

            cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                        "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, MasterData.Ord_id, Boxes._id, bm._id, Prods._id, Prods.sentToMasterDate, Boxes.archive" +
                        " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s Where Opers._id=" + AppController.getInstance().getDefs().get_Id_o() +
                        " and bm.Id_o=Opers._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and bm._id=Prods.Id_bm" +
                        " and Prods.Id_d=Deps._id and Prods.Id_s=s._id " +
                        //" and ((Prods.sentToMasterDate IS NULL) OR (Prods.sentToMasterDate=''))" +
                        addWhereOutDoc +
                        addWhereDepartment +
                        addWhereSentToMasterDate +
                        " Order by MasterData.Ord_id,  Boxes.N_box", null);

            while (cursor.moveToNext()) {
                Log.d(TAG, "listboxes -> ".concat(String.valueOf(cursor.getCount())) );
                HashMap readBox = new HashMap<String, Integer>();
                String sTmp;
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
                readBox.put("sent", (cursor.getType(14) == FIELD_TYPE_NULL) ? "N" : "Y" + "/sent");
                readBox.put("arch", cursor.getInt(cursor.getColumnIndex("archive")) != 0 ? "N" : "Y" + "/arch");
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
    public foundBox searchBox(final int Order_id, final String storedbarcode) {
        Cursor cursor = null;
        foundBox fb = new foundBox();
        try {
            fb.setNB(Integer.valueOf( StringUtils.substringAfterLast(storedbarcode, ".") ));
            fb.setBarcode(storedbarcode);
            fb.setBoxdef("№ кор: ".concat(String.valueOf(fb.getNB()))+". ");
            fb.setQB(1);

            SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

            String query = "SELECT Boxes._id, archive FROM Boxes Where Boxes.Id_m=? and Boxes.N_box=?" ;
            cursor = mDataBase.rawQuery(query, new String [] {String.valueOf(Order_id), String.valueOf(fb.getNB())});

            if (cursor != null && cursor.moveToFirst()) {
                fb.set_id(cursor.getString(cursor.getColumnIndex("_id")));
                fb.set_archive (cursor.getInt(cursor.getColumnIndex("archive")) != 0);
            }
        }catch (Exception e) {
            Log.e(TAG, "getOutDocNotSent -> ".concat(e.getMessage()) );
            return fb;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
        return fb;
    }

}
