package com.example.yg.wifibcscaner.data.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.controller.AppController;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getLongDateTimeString;
import static com.example.yg.wifibcscaner.utils.StringUtils.retStringFollowingCRIfNotNull;

public class OrderService {
    //list all boxes
    public ArrayList<HashMap<String, String>> listorders() {
        ArrayList<HashMap<String, String>> readOrders = new ArrayList<HashMap<String, String>>();
//        Cursor cursor = mDb.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord,MasterData.Q_box, Boxes.N_box, sum (Prods.RQ_box)" +
//                " FROM Boxes, Prods, Deps, MasterData Where Boxes.Id_o=1 and Boxes.Id_m=MasterData._id and Boxes._id=Prods.Id_b and Prods.Id_d=Deps._id", null);
        SQLiteDatabase db = AppController.getInstance().getDbHelper().openDataBase(false);
        Cursor cursor = db.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord,MasterData.Q_box, " +
                "MasterData.N_box, MasterData.DT " +
                "FROM MasterData WHERE division_code=? ORDER BY MasterData._id DESC", new String [] {String.valueOf(AppController.getInstance().getDbHelper().defs.getDivision_code())});
        cursor.moveToFirst();

//Пробегаем по всем коробкам
        while (!cursor.isAfterLast()) {
            HashMap readOrder = new HashMap<String, String>();
            //Заполняем
            readOrder.put("Ord", cursor.getString(0) + ". " + cursor.getString(1));
            readOrder.put("Cust", cursor.getString(2) + "\n" + retStringFollowingCRIfNotNull(cursor.getString(3)) +
                    "Заказ: " + cursor.getString(4) + ". Коробок" +
                    ": " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + "\n" +
                    " Загружен: " + getLongDateTimeString(cursor.getLong(7)));

            //Закидываем в список
            readOrders.add(readOrder);

            //Переходим к следующеq
            cursor.moveToNext();
        }
        cursor.close();
        AppController.getInstance().getDbHelper().closeDataBase();

        return readOrders;
    }
}
