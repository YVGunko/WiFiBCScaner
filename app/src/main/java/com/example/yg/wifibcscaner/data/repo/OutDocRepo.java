package com.example.yg.wifibcscaner.data.repo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.yg.wifibcscaner.utils.AppUtils.isDepAndSotrOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeString;

public class OutDocRepo {
    private static final String TAG = "sProject -> OutDocRepo";
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
            if (userRepo.checkSuperUser(defs.get_idUser())) {
                cursor = mDataBase.rawQuery(queryNextOutDocNumber,
                        new String[]{String.valueOf(defs.getDivision_code()), String.valueOf(defs.get_Id_o()),
                                dateToStartSince});
            } else {
                cursor = mDataBase.rawQuery(queryNextOutDocNumberForUser,
                        new String[]{String.valueOf(defs.getDivision_code()), String.valueOf(defs.get_Id_o()), String.valueOf(defs.get_idUser()),
                                dateToStartSince});
            }

            return ( cursor.getInt(0) + 1 );

        } catch (Exception e) {
            Log.w(TAG, "getNextOutDocNumber -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int outDocsAddRec () {
        if (isDepAndSotrOper(defs.get_Id_o())) {
            if (defs.get_Id_o() <= 0 || defs.get_Id_d() <= 0 || defs.get_Id_s() <= 0)
                return 0;
        } else {
            if (defs.get_Id_o() <= 0)
                return 0;
        }

        final int nextOutDocNumber = getNextOutDocNumber();
        if (nextOutDocNumber == 0) return 0;

        try {
            try{
                OutDocs outDoc = prepareOutDoc(nextOutDocNumber,
                        isDepAndSotrOper(defs.get_Id_o()) ? defs.get_Id_d() : 0,
                        isDepAndSotrOper(defs.get_Id_o()) ? defs.get_Id_s() : 0,
                        getDayTimeString(new Date().getTime()));

                if (createOutDocsForCurrentOperInBulk(Collections.singletonList(outDoc))) {
                    currentOutDoc = outDoc;
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

        for (Integer depId : depRepo.getAllDepartmentIdByDivisionCodeAndOperationId(defs.getDivision_code(), defs.get_Id_o())){
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

        for (Sotr sotr : getSotrIdByDep(defs.getDivision_code(), defs.get_Id_o(), defs.get_Id_d())){
            if (sotr.get_id() != 0) {
                listOutDocs.add(prepareOutDoc(nextOutDocNumber, defs.get_Id_d(), defs.descDep, sotr.get_id(), sotr.get_Sotr(), dateToSet));
                nextOutDocNumber++;
            }
        }

        return createOutDocsForCurrentOperInBulk(listOutDocs);
    }
    private OutDocs prepareOutDoc (final int outDocNumber, final int depId, final int sotrId, final String dateToSet){
        String sotrName = (sotrId != 0) ? getSotr_Name_by_id(sotrId) : "";
        if (StringUtils.isNotEmpty(sotrName)) sotrName = sotrName.substring(0, sotrName.indexOf(" "));

        String description = (depId == 0 & sotrId == 0)
                ? defs.descOper.concat(", ").concat(defs.descUser)
                : (depRepo.getDepNameById(depId).concat(", ").concat(sotrName));

        OutDocs outDoc = new OutDocs(getUUID(), defs.get_Id_o(), outDocNumber,
                description,
                dateToSet, defs.getDivision_code(), defs.get_idUser(),
                sotrId, depId);
        return outDoc;
    }
    private OutDocs prepareOutDoc (final int outDocNumber, final int depId, final String depName, final int sotrId, final String sotrName, final String dateToSet){
        OutDocs outDoc = new OutDocs(getUUID(), defs.get_Id_o(), outDocNumber,
                ((StringUtils.isNotEmpty(depName) ? depName : "") + (StringUtils.isNotEmpty(sotrName) ? ", "+sotrName : "")),
                dateToSet, defs.getDivision_code(), defs.get_idUser(),
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
}
