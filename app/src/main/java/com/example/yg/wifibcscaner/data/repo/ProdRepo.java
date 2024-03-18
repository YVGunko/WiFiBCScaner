package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateToLong;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getUUID;

public class ProdRepo {
    private static final String TAG = "sProject -> ProdRepo.";

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
