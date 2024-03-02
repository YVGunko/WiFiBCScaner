package com.example.yg.wifibcscaner.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;

import java.util.Optional;

import static com.example.yg.wifibcscaner.utils.AppUtils.getFirstOperFor;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;

public class DefsRepo {
    private static final String TAG = "sProject -> DefsRepo.";
    private SQLiteDatabase mDataBase = AppController.getInstance().getDbHelper().openDataBase();
    private final OperRepo operRepo = new OperRepo();
    private final DivisionRepo divRepo = new DivisionRepo();
    private final DepartmentRepo depRepo = new DepartmentRepo();
    private final SotrRepo sotrRepo = new SotrRepo();
    private final UserRepo userRepo = new UserRepo();

    public Optional<Defs> selectDefsTable(){
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Id_d,Id_o,Id_s,Host_IP,Port,idOperFirst,idOperLast,division_code,idUser,DeviceId  FROM Defs ", null);
            if (cursor != null && cursor.moveToFirst()) {
                Defs defs = new Defs(cursor.getInt(0),cursor.getInt(1),cursor.getInt(2),
                        cursor.getString(3),cursor.getString(4),cursor.getInt(5),
                        cursor.getInt(6),cursor.getString(7),cursor.getInt(8),cursor.getString(9));
                defs.setDescOper ( operRepo.getOperNameById(defs.get_Id_o()) );
                defs.setDescDep ( depRepo.getDepNameById(defs.get_Id_d()) );
                defs.setDescSotr ( sotrRepo.getNameById(defs.get_Id_s()) );
                defs.setDescDivision ( divRepo.getDivisionNameByCode(defs.getDivision_code()) );
                defs.setDescUser ( userRepo.getUserName(defs.get_idUser()) );
                defs.setDescFirstOperForCurrent ( operRepo.getOperNameById(getFirstOperFor(defs.get_Id_o())) );
                //AppController.getInstance().setDefs(defs);
                return Optional.ofNullable(defs);
            }
            return Optional.empty();
        }catch (Exception e) {
            Log.e(TAG, "selectDefsTable -> ".concat(e.getMessage()));
            return Optional.empty();
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int updateDefsTable(Defs defs){
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Defs.COLUMN_Id_d, defs.get_Id_d());
            values.put(Defs.COLUMN_Id_o, defs.get_Id_o());
            values.put(Defs.COLUMN_Id_s, defs.get_Id_s());
            if (defs.get_idUser()!=0) values.put(Defs.COLUMN_idUser, defs.get_idUser());
            values.put(Defs.COLUMN_Host_IP, defs.get_Host_IP());
            values.put(Defs.COLUMN_Port, defs.get_Port());
            values.put(Defs.COLUMN_Division_code, defs.getDivision_code());
            values.put(Defs.COLUMN_DeviceId, defs.getDeviceId());
            String strFilter = "_id=1" ;
            return mDataBase.update(Defs.table_Defs, values,strFilter, null);
        } catch (SQLException e) {
            return 0;
        }
    }
}
