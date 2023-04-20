package com.example.yg.wifibcscaner.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.service.OutDocService;

import java.util.Date;

import static com.example.yg.wifibcscaner.data.service.OutDocService.makeOutDocBoxAndProdDesc;
import static com.example.yg.wifibcscaner.data.service.OutDocService.makeOutDocDesc;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getLongDateTimeString;
import static com.example.yg.wifibcscaner.utils.DbUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getUUID;

import static java.lang.String.valueOf;

public class OutDocRepo {
    OutDocService outDocService;
    private static final String TAG = "OutDocRepo";
    private static final String MY_CHANNEL_ID = "Data Exchange";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
    private DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

    public String selectOutDocById (@NonNull String id){
        Cursor cursor = null;
        boolean dbWasOpen = false;
        String result = makeOutDocBoxAndProdDesc("0", "0");;
        try {
            cursor = mDataBase.rawQuery("select p.idOutDocs, count(p.Id_bm) as boxNumber, sum(p.RQ_box) as RQ_box" +
                    " FROM Prods p" +
                    " where p.idOutDocs='"+id+"' "+
                    " group by p.idOutDocs", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();

                result = makeOutDocBoxAndProdDesc(cursor.getString(1), cursor.getString(2));
            }else {
                Log.d(TAG, "selectOutDocById rawQuery result is null!" );
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!dbWasOpen) mDataBase.close();
            return result;
        }
    }

    public String selectCurrentOutDocDetails (){
        if (mDbHelper.currentOutDoc.get_id() != null) {
            Cursor cursor = null;
            boolean dbWasOpen = false;
            String result = "Кор.:0, Подошвы:0";
            try {
                cursor = mDataBase.rawQuery("select p.idOutDocs, count(p.Id_bm) as boxNumber, sum(p.RQ_box) as RQ_box" +
                        " FROM Prods p" +
                        " where p.idOutDocs='"+AppController.getInstance().getDbHelper().currentOutDoc.get_id()+"' "+
                        " group by p.idOutDocs", null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    result = makeOutDocDesc(new String[]{valueOf(AppController.getInstance().getDbHelper()
                            .currentOutDoc.get_number()), AppController.getInstance().getDbHelper()
                            .currentOutDoc.get_DT(), cursor.getString(1), cursor.getString(2)});
                }else {
                    Log.d(TAG, "selectCurrentOutDocDetails rawQuery result is null!" );
                }
            } finally {
                tryCloseCursor(cursor);
                //if (!dbWasOpen) mDataBase.close();
                return result;
            }
        } else {
            return makeOutDocDesc(new String[]{null});
        }
    }

    public long insertOrUpdateOutDocs(OutDocs outdocs) {
        long l = 0;
        try {
            try {
                //mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                ContentValues values = new ContentValues();
                values.clear();
                values.put(OutDocs.COLUMN_Id, outdocs.get_id());
                values.put(OutDocs.COLUMN_Id_o, outdocs.get_Id_o());
                values.put(OutDocs.COLUMN_number, outdocs.get_number());
                values.put(OutDocs.COLUMN_comment, outdocs.get_comment());
                values.put(OutDocs.COLUMN_DT, getDateTimeLong(outdocs.get_DT()));
                values.put(AppController.getInstance().getDbHelper().COLUMN_sentToMasterDate, new Date().getTime());
                values.put(OutDocs.COLUMN_Division_code, outdocs.getDivision_code());
                values.put(OutDocs.COLUMN_idUser, outdocs.getIdUser());
                l = mDataBase.insertWithOnConflict(OutDocs.TABLE, null, values, 5);
            } catch (SQLException e) {
                // TODO: handle exception
                throw e;
            }
        }finally {
            //mDataBase.close();
            return l;
        }
    }


    public boolean outDocsAddRec () {
        Cursor cursor = null;
        int docNum = 0;
        try {
            try{
                mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                if (AppController.getInstance().getDbHelper().checkSuperUser(mDbHelper.defs.get_idUser())){
                    String query = "SELECT max(number) FROM OutDocs where division_code=? and Id_o=?";
                    cursor = mDataBase.rawQuery(query,
                            new String [] {String.valueOf(mDbHelper.defs.getDivision_code()), String.valueOf(mDbHelper.defs.get_Id_o())});
                }
                else {
                    String query = "SELECT max(number) FROM OutDocs where division_code=? and Id_o=? and idUser=?";
                    cursor = mDataBase.rawQuery(query,
                            new String [] {String.valueOf(mDbHelper.defs.getDivision_code()), String.valueOf(mDbHelper.defs.get_Id_o()), String.valueOf(mDbHelper.defs.get_idUser())});
                }
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst(); //есть boxes & prods
                    docNum = cursor.getInt(0);
                    ContentValues values = new ContentValues();
                    values.clear();
                    String uuid = getUUID();
                    Long date = new Date().getTime();
                    values.put(OutDocs.COLUMN_Id, uuid);
                    values.put(OutDocs.COLUMN_number, docNum+1);
                    values.put(OutDocs.COLUMN_comment, mDbHelper.defs.descDep+", "+mDbHelper.defs.descUser);
                    values.put(OutDocs.COLUMN_DT, date);
                    values.put(OutDocs.COLUMN_Id_o, mDbHelper.defs.get_Id_o());
                    values.put(OutDocs.COLUMN_Division_code, mDbHelper.defs.getDivision_code());
                    values.put(OutDocs.COLUMN_idUser, mDbHelper.defs.get_idUser());
                    if (mDataBase.insertOrThrow(OutDocs.TABLE, null, values)>0) {
                        mDbHelper.currentOutDoc.set_id(uuid);
                        mDbHelper.currentOutDoc.set_number(docNum+1);
                        mDbHelper.currentOutDoc.set_comment(mDbHelper.defs.descDep+", "+mDbHelper.defs.descUser);
                        mDbHelper.currentOutDoc.set_DT(getLongDateTimeString(date));
                        mDbHelper.currentOutDoc.set_Id_o(mDbHelper.defs.get_Id_o());
                        mDbHelper.currentOutDoc.setDivision_code(mDbHelper.defs.getDivision_code());
                        mDbHelper.currentOutDoc.setIdUser(mDbHelper.defs.get_idUser());
                    }
                }
            }catch (Exception e) {
                // TODO: handle exception
                throw e;
            }
        }  finally {
            tryCloseCursor(cursor);
            return (docNum != 0);
        }
    }



    public Cursor listOutDocs() {
        Cursor cursor = null;
        try {
            Log.d(TAG, "listOutDocs cursor is NULL! "+mDataBase.isOpen() );
            if (mDbHelper.checkSuperUser(mDbHelper.defs.get_idUser())) {
                //if (!mDataBase.isOpen())
                //    mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT _id, number, comment," +
                                " strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser," +
                                " 'Нкл. ' || cast(number as text) || ' от ' || strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as numberAndDate" +
                                " FROM OutDocs where _id<>0 and division_code=? and Id_o=?" +
                                " ORDER BY number desc",
                        new String[]{String.valueOf(mDbHelper.defs.getDivision_code()), String.valueOf(mDbHelper.defs.get_Id_o())});
            }
            else {
                //if (!mDataBase.isOpen())
                //    mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT _id, number, comment," +
                                " strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser ," +
                                " 'Нкл. ' || cast(number as text) || ' от ' || strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as numberAndDate" +
                                " FROM OutDocs where _id<>0 and division_code=? and Id_o=? and idUser=?" +
                                " ORDER BY number desc",
                        new String[]{String.valueOf(mDbHelper.defs.getDivision_code()), String.valueOf(mDbHelper.defs.get_Id_o()), String.valueOf(mDbHelper.defs.get_idUser())});
            }
        } finally {
            if (cursor != null) {
                cursor.moveToFirst();
                Log.d(TAG, "listOutDocs cursor is not null! Record count = " + cursor.getCount() );
            }else {
                Log.d(TAG, "listOutDocs cursor is NULL! " );
                //mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT _id, number, comment," +
                                " strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser ," +
                                " 'Нкл. ' || cast(number as text) || ' от ' || strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as numberAndDate" +
                                " FROM OutDocs where _id=0",
                        null);
            }
            return cursor;
        }
    }
}
