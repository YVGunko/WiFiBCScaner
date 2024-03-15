package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.service.foundOrder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getOrder_id;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.makeOrderdef;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.retStringFollowingCRIfNotNull;

public class OrderRepo {
    private static final String TABLE_MD = "MasterData";
    private static final String TAG = "sProject -> OrderRepo.";
    private SQLiteDatabase mDataBase ;

    public foundOrder searchOrder(String storedbarcode) {

        String Order_Id = getOrder_id(storedbarcode);  // по dot
        Cursor c;
        foundOrder fo = new foundOrder();
        if (StringUtils.isNotBlank(Order_Id)) {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            String query = "SELECT _id,Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box,DT,archive, division_code FROM " + TABLE_MD + " WHERE Ord_id = '" + Order_Id + "'";
            c = mDataBase.rawQuery(query, null);
            try {
                if (c != null && c.moveToFirst()) {
                    fo.set_id( c.getInt(0) );
                    fo.setQO( c.getInt(6) );
                    fo.setQB( c.getInt(7) );
                    fo.setNB( c.getInt(8) );
                    fo.setDT( lDateToString(c.getLong(9)) );
                    fo.setOrderdef( makeOrderdef(c) );
                    fo.setBarcode( storedbarcode );
                    fo.setArchive( (c.getInt(c.getColumnIndex("archive")) != 0) );
                    fo.setDivision_code( c.getString(c.getColumnIndex("division_code")) );
                }
            } catch (Exception e) {
                Log.e(TAG, "searchOrder -> ".concat(e.getMessage()) );
                return fo;
            } finally {
                tryCloseCursor(c);
            }
        }
        return fo;
    }
    public ArrayList<HashMap<String, String>> listorders() {
        ArrayList<HashMap<String, String>> readOrders = new ArrayList<HashMap<String, String>>();
        Cursor cursor = null;
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord,MasterData.Q_box, " +
                    "MasterData.N_box, MasterData.DT " +
                    "FROM MasterData WHERE division_code=? ORDER BY MasterData._id DESC", new String [] {String.valueOf(AppController.getInstance().getDefs().getDivision_code())});
            while (cursor.moveToNext()) {
                HashMap readOrder = new HashMap<String, String>();
                //Заполняем
                readOrder.put("Ord", cursor.getString(0) + ". " + cursor.getString(1));
                readOrder.put("Cust", cursor.getString(2) + "\n" + retStringFollowingCRIfNotNull(cursor.getString(3)) +
                        "Заказ: " + cursor.getString(4) + ". Коробок" +
                        ": " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + "\n" + " Загружен: " + lDateToString(cursor.getLong(7)));
                readOrders.add(readOrder);
            }

        }catch (Exception e){
            Log.e (TAG, "listorders -> ".concat(e.getMessage()));
            HashMap readBox = new HashMap<String, String>();
            readBox.put("Ord", "Ошибка!");
            readBox.put("Cust", "Ошибка!");
            readOrders.add(readBox);
        }finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
        return readOrders;
    }
    public String getOrderUpdateDate(@NonNull String globalUpdateDate){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Orders ", null);
            if (cursor != null && cursor.moveToFirst()) {
                return lDateToString(cursor.getLong(0) > sDateTimeToLong(globalUpdateDate) ? cursor.getLong(0) : sDateTimeToLong(globalUpdateDate));
            }
            return globalUpdateDate;
        }catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public String getOrderUpdateDate(@NonNull String globalUpdateDate){
        try {
            if (SharedPrefs.getInstance() != null) {
                long saved = SharedPrefs.getInstance().getNextUpdateDate();
                return lDateToString(saved > sDateTimeToLong(globalUpdateDate) ? saved : sDateTimeToLong(globalUpdateDate));
            }

            return globalUpdateDate;
        }catch (Exception e) {
            Log.e(TAG, "getMaxDepsDate -> ".concat(e.getMessage()));
            return globalUpdateDate;
        }
    }*/
    public void insertOrdersInBulk(List<Orders> list){
        try {
            //mDataBase.execSQL("PRAGMA foreign_keys = 0;");
            mDataBase.beginTransaction();
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

            mDataBase.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
            //mDataBase.execSQL("PRAGMA foreign_keys = 1;");
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
        }
    }
}
