package com.example.yg.wifibcscaner.data.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.utils.DbUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

public class CurrentDocDetailsRepository {
    private static final String TAG = "CurrentDocDetailsRepos";
    SQLiteDatabase db;

    public void setCurrentOutDocDetails (String id){
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            Cursor cursor = null;
            boolean dbWasOpen = false;
            String result = "";
            try {
                if (!db.isOpen()) {
                    db = AppController.getInstance().getDbHelper().getReadableDatabase();
                } else dbWasOpen = true;
                cursor = db.rawQuery("select p.idOutDocs, count(bm.Id_b) as boxNumber, sum(p.RQ_box) as RQ_box" +
                        " FROM Prods p, BoxMoves bm" +
                        " where p.idOutDocs='"+id+"' and bm._id=p.Id_bm"+
                        " group by p.idOutDocs", null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    result = "Кор: "+cursor.getString(1)+", Под.: "+cursor.getString(2);
                }else {
                    result = "Кор.:0, Подошвы:0";
                }
            } catch (Exception e) {
                Log.e(TAG, ". setCurrentOutDocDetails -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
            } finally {
                DbUtils.tryCloseCursor(cursor);
                if (!dbWasOpen) db.close();
            }
        });
    }

}
