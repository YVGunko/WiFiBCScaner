package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.DataBaseHelper.COLUMN_sentToMasterDate;
import static com.example.yg.wifibcscaner.utils.AppUtils.isDepAndSotrOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getUUID;

public class OutDocRepo {
    private static final String TAG = "sProject -> OutDocRepo.";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();

    private final UserRepo userRepo = new UserRepo();
    private final SotrRepo sotrRepo = new SotrRepo();
    private final OperRepo operRepo = new OperRepo();
    private final DivisionRepo divRepo = new DivisionRepo();
    private final DepartmentRepo depRepo = new DepartmentRepo();

    /*
     * OutDoc add, add in bulk, next number
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getNextOutDocNumber () {
        final String queryNextOutDocNumber = "SELECT max(number) FROM OutDocs where division_code=? and Id_o=? AND DT >=? ";
        final String queryNextOutDocNumberForUser = "SELECT max(number) FROM OutDocs where division_code=? and Id_o=? and idUser=? AND DT >=? ";

        String dateToStartSince = String.valueOf(DateTimeUtils.getFirstDayOfYear());
        if (SharedPrefs.getInstance() != null) {
            dateToStartSince = String.valueOf(SharedPrefs.getInstance().getOutdocsNumerationStartDate());
        }

        Cursor cursor = null;

        try {
            if (userRepo.checkSuperUser(AppController.getInstance().getDefs().get_idUser())) {
                cursor = mDataBase.rawQuery(queryNextOutDocNumber,
                        new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code()),
                                String.valueOf(AppController.getInstance().getDefs().get_Id_o()),
                                dateToStartSince});
            } else {
                cursor = mDataBase.rawQuery(queryNextOutDocNumberForUser,
                        new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code()),
                                String.valueOf(AppController.getInstance().getDefs().get_Id_o()),
                                String.valueOf(AppController.getInstance().getDefs().get_idUser()),
                                dateToStartSince});
            }
            if (cursor != null && cursor.moveToFirst()) {
                return (cursor.getInt(0) + 1);
            }
            Log.i(TAG, "getNextOutDocNumber -> cursor is empty, gonna return 1");
            return 1;
        } catch (Exception e) {
            Log.w(TAG, "getNextOutDocNumber -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int outDocsAddRec () {
        if (isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o())) {
            if (AppController.getInstance().getDefs().get_Id_o() <= 0 || AppController.getInstance().getDefs().get_Id_d() <= 0 || AppController.getInstance().getDefs().get_Id_s() <= 0)
                return 0;
        } else {
            if (AppController.getInstance().getDefs().get_Id_o() <= 0)
                return 0;
        }

        final int nextOutDocNumber = getNextOutDocNumber();
        if (nextOutDocNumber == 0) return 0;

        try {
            try{
                OutDocs outDoc = prepareOutDoc(nextOutDocNumber,
                        isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o()) ? AppController.getInstance().getDefs().get_Id_d() : 0,
                        isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o()) ? AppController.getInstance().getDefs().get_Id_s() : 0,
                        getDayTimeString(new Date().getTime()));

                if (createOutDocsForCurrentOperInBulk(Collections.singletonList(outDoc))) {
                    AppController.getInstance().setCurrentOutDoc( outDoc );
                }
            }catch (Exception e) {
                Log.e(TAG, "outDocsAddRec exception -> ",e);
            }
        }  finally {
            return nextOutDocNumber;
        }
    }
    public boolean createOutDocsForCurrentOper(int nextOutDocNumber) {
        //for current Div and Oper select all Deps and first Sotr of the Dep.
        List<OutDocs> listOutDocs = new ArrayList<>();
        int sotrId;
        final String dateToSet = getDayTimeString(new Date());

        for (Integer depId : depRepo.getAllDepartmentIdByDivisionCodeAndOperationId(AppController.getInstance().getDefs().getDivision_code(), AppController.getInstance().getDefs().get_Id_o())){
            sotrId = sotrRepo.getOneSotrIdByDepId(depId);
            if (sotrId != 0) {
                listOutDocs.add(prepareOutDoc(nextOutDocNumber, depId, sotrId, dateToSet));
                nextOutDocNumber++;
            }
        }

        return createOutDocsForCurrentOperInBulk(listOutDocs);
    }
    public boolean createOutDocsForCurrentDep(int nextOutDocNumber) {
        //for current Div and Oper and Dep select all Sotr.
        List<OutDocs> listOutDocs = new ArrayList<>();
        final String dateToSet = getDayTimeString(new Date());

        for (Sotr sotr : sotrRepo.getSotrIdByDivisionCodeAndOperationIdAndDepartmentId(AppController.getInstance().getDefs().getDivision_code(), AppController.getInstance().getDefs().get_Id_o(), AppController.getInstance().getDefs().get_Id_d())){
            if (sotr.get_id() != 0) {
                listOutDocs.add(prepareOutDoc(nextOutDocNumber, AppController.getInstance().getDefs().get_Id_d(), AppController.getInstance().getDefs().getDescDep(), sotr.get_id(), sotr.get_Sotr(), dateToSet));
                nextOutDocNumber++;
            }
        }

        return createOutDocsForCurrentOperInBulk(listOutDocs);
    }
    private OutDocs prepareOutDoc (final int outDocNumber, final int depId, final int sotrId, final String dateToSet){
        String sotrName = (sotrId != 0) ? sotrRepo.getNameById(sotrId) : "";
        if (StringUtils.isNotEmpty(sotrName)) sotrName = sotrName.substring(0, sotrName.indexOf(" "));

        String description = (depId == 0 & sotrId == 0)
                ? AppController.getInstance().getDefs().getDescDep().concat(", ").concat(AppController.getInstance().getDefs().getDescUser())
                : (depRepo.getDepNameById(depId).concat(", ").concat(sotrName));

        OutDocs outDoc = new OutDocs(getUUID(), AppController.getInstance().getDefs().get_Id_o(), outDocNumber,
                description,
                dateToSet, AppController.getInstance().getDefs().getDivision_code(), AppController.getInstance().getDefs().get_idUser(),
                sotrId, depId);
        return outDoc;
    }
    private OutDocs prepareOutDoc (final int outDocNumber, final int depId, final String depName, final int sotrId, final String sotrName, final String dateToSet){
        OutDocs outDoc = new OutDocs(getUUID(), AppController.getInstance().getDefs().get_Id_o(), outDocNumber,
                ((StringUtils.isNotEmpty(depName) ? depName : "") + (StringUtils.isNotEmpty(sotrName) ? ", "+sotrName : "")),
                dateToSet, AppController.getInstance().getDefs().getDivision_code(), AppController.getInstance().getDefs().get_idUser(),
                sotrId, depId);
        return outDoc;
    }
    private boolean createOutDocsForCurrentOperInBulk(List<OutDocs> list){
        boolean result = false;
        try {

            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO OutDocs (_id, Id_o, number, comment, DT, division_code, idUser, idSotr, idDeps) " +
                    " VALUES (?,?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (OutDocs o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindLong(2, o.get_Id_o());
                statement.bindLong(3, o.get_number());
                if (o.get_comment() == null)
                    statement.bindString(4, "");
                else
                    statement.bindString(4, o.get_comment());
                statement.bindLong(5, getDateTimeLong(o.get_DT()));
                statement.bindString(6, o.getDivision_code());
                statement.bindLong(7, o.getIdUser());
                statement.bindLong(8, o.getIdSotr());
                statement.bindLong(9, o.getIdDeps());
                statement.executeInsert();
            }

            mDataBase.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
            result = true;
        } catch (Exception e) {
            Log.e(TAG, "createOutDocsForCurrentOperInBulk exception -> ", e);
        } finally {
            mDataBase.endTransaction();
            return result;
        }
    }
    public boolean updateOutDocsetSentToMasterDate (OutDocs od) {
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(COLUMN_sentToMasterDate, new Date().getTime());
            return  (mDataBase.update(OutDocs.TABLE, values,OutDocs.COLUMN_Id +"='"+od.get_id()+"'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e( TAG, "updateOutDocsetSentToMasterDate exception ".concat(e.getMessage()) );
            MessageUtils.showToast(AppController.getInstance().getContext(),
                    "Запись даты отправки накладных. Операция не выполнена!",
                    false);
            return false;
        }
    }
    public void insertOutDocInBulk(List<OutDocs> list){
        try {
            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO OutDocs (_id, Id_o, number, comment, DT, sentToMasterDate, division_code, idUser) " +
                    " VALUES (?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (OutDocs o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindLong(2, o.get_Id_o());
                statement.bindLong(3, o.get_number());
                if (o.get_comment() == null)
                    statement.bindString(4, "");
                else
                    statement.bindString(4, o.get_comment());

                statement.bindLong(5, getDateTimeLong(o.get_DT()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(6, new Date().getTime());
                else
                    statement.bindLong(6, getDateTimeLong(o.get_sentToMasterDate()));

                statement.bindString(7, o.getDivision_code());
                statement.bindLong(8, o.getIdUser());
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
    public ArrayList<OutDocs> getOutDocNotSent(){
        Cursor cursor = null;
        ArrayList<OutDocs> readBoxMoves = new ArrayList<OutDocs>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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
            return readBoxMoves;
        }catch (Exception e) {
            Log.e(TAG, "getOutDocNotSent -> ".concat(e.getMessage()) );
            return readBoxMoves;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String selectCurrentOutDocDetails (String id){
        if (StringUtils.isEmpty(id)) return "";
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("select p.idOutDocs, count(bm.Id_b) as boxNumber, sum(p.RQ_box) as RQ_box" +
                    " FROM Prods p, BoxMoves bm" +
                    " where p.idOutDocs='"+id+"' and bm._id=p.Id_bm"+
                    " group by p.idOutDocs", null);
            if (cursor != null && cursor.moveToFirst()) {
                return ", Кор: "+cursor.getString(1)+", Под.: "+cursor.getString(2);
            }
            return ", Кор: 0";
        } catch (Exception e){
            return ". Нет данных.";
        } finally {
            tryCloseCursor(cursor);
            AppController.getInstance().getDbHelper().closeDataBase();
        }
    }
    public Cursor listOutDocs() {
        Date curDate = new Date();
        long dateFrom = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(curDate, 1));
        long dateTill = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(curDate, 1));
        if (SharedPrefs.getInstance() != null) {
            dateFrom = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(curDate, -SharedPrefs.getInstance().getOutDocsDays()+1));
        }
        Cursor cursor;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            if (userRepo.checkSuperUser(AppController.getInstance().getDefs().get_idUser())) {
                cursor = mDataBase.rawQuery("SELECT _id, number, comment, strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser, idSotr, idDeps, DT as dtorder " +
                                " FROM OutDocs where _id<>0 and division_code=? and Id_o=?" +
                                " AND DT BETWEEN "+dateFrom+
                                " AND "+dateTill+
                                " ORDER BY dtorder desc, number desc",
                        new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code()),
                                String.valueOf(AppController.getInstance().getDefs().get_Id_o())});
            }
            else {
                // for what in the world I put date(DT / 1000,'unixepoch') here ???
                /*                                    " AND date(DT / 1000,'unixepoch') BETWEEN date("+dateFrom+
                                " / 1000,'unixepoch') AND  date("+dateTill+" / 1000,'unixepoch')"+
                * */
                cursor = mDataBase.rawQuery("SELECT _id, number, comment, strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser, idSotr, idDeps, DT as dtorder " +
                                " FROM OutDocs where _id<>0 and division_code=? and Id_o=? and idUser=?" +
                                " AND DT BETWEEN "+dateFrom+
                                " AND "+dateTill+
                                " ORDER BY dtorder desc, number desc",
                        new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code()),
                                String.valueOf(AppController.getInstance().getDefs().get_Id_o()), String.valueOf(AppController.getInstance().getDefs().get_idUser())});
            }
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            // TODO fill cursor manually
            Log.e(TAG, "listOutDocs cursor is NULL! ".concat(e.getMessage()) );
            cursor = mDataBase.rawQuery("SELECT _id, number, comment, strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser, idSotr, idDeps, DT as dtorder " +
                            " FROM OutDocs where _id=0",
                    null);
            return cursor;
        }
    }

}
