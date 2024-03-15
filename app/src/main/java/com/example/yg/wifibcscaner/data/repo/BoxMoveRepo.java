package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;

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
    public boolean insertBoxMoves(BoxMoves bm) {
        Cursor cursor = null;
        SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            cursor = mDataBase.rawQuery("SELECT bm._id as _id FROM BoxMoves bm Where bm.Id_o=" + bm.get_Id_o() + " and bm.Id_b='" + bm.get_Id_b()+"'", null);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    return StringUtils.isNotBlank(cursor.getString(0));
                }catch (Exception e){
                    return false;
                }

            } else {
                ContentValues values = new ContentValues();
                values.clear();
                values.put(BoxMoves.COLUMN_ID, bm.get_id());
                values.put(BoxMoves.COLUMN_Id_b, bm.get_Id_b());
                values.put(BoxMoves.COLUMN_Id_o, bm.get_Id_o());
                values.put(BoxMoves.COLUMN_DT, sDateTimeToLong(bm.get_DT()));
                if (bm.get_sentToMasterDate() != null) values.put(BoxMoves.COLUMN_sentToMasterDate, sDateTimeToLong(bm.get_sentToMasterDate()));
                return mDataBase.insertWithOnConflict(BoxMoves.TABLE_bm, null, values, 5) > 0;
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
}
