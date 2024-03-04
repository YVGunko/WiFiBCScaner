package com.example.yg.wifibcscaner.data.repo;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;

import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;

public class BoxMoveRepo {
    private static final String TAG = "sProject -> BoxMoveRepo.";
    public void insertBoxMoveInBulk(List<BoxMoves> list) {
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            mDataBase.beginTransaction();
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

            mDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
        }
    }
}
