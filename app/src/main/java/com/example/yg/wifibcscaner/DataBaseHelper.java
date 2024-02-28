package com.example.yg.wifibcscaner;

/**
 * Created by yg on 12.12.2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.activity.lastUpdate;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.service.spBarcode;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import static android.text.TextUtils.substring;

import static com.example.yg.wifibcscaner.utils.AppUtils.getFirstOperFor;
import static com.example.yg.wifibcscaner.utils.AppUtils.isDepAndSotrOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.isNotEmpty;
import static com.example.yg.wifibcscaner.utils.AppUtils.isOneOfFirstOper;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateToLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper";

    private static String DB_PATH = "";
    private static String DB_NAME = "SQR.db";
    private static final String TABLE_MD = "MasterData";

    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";
    public long serverUpdateTime;
    public static final String puDivision = "00-000002";
    public static final String tepDivision = "00-000025";

    private SQLiteDatabase mDataBase;
    private AtomicInteger mOpenCounter = new AtomicInteger();

    public static final Long ldtMin = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(new Date(), -DateTimeUtils.numberOfDaysInMonth(new Date())));
    public static final String dtMin = DateTimeUtils.getStartOfDayString(ldtMin);

    public Defs defs;
    public static OutDocs currentOutDoc;
    private final OperRepo operRepo = new OperRepo();
    private final DivisionRepo divRepo = new DivisionRepo();
    private final DepartmentRepo depRepo = new DepartmentRepo();

    public class foundbox {
        String barcode; //строка описания
        String boxdef; //строка описания
        int QB; //количество в коробке
        int NB; //# коробa
        int RQ; //принятое количество всего в коробке
        String _id;
        String outDocs;
        String depSotr;
        boolean _archive;
    }

    public class foundorder {
        String barcode; //строка описания
        String orderdef; //строка описания
        int _id; //
        String Ord_Id;
        String Ord;
        String Cust;
        String Nomen;
        String Attrib;
        int QO; //количество в заказе
        int QB; //количество в коробке
        int NB; //Количество коробок
        String DT;
        String division_code;
        boolean archive;
    }
    private String[] splitBarcode(String storedbarcode) {
        String[] atmpBarcode = storedbarcode.split("[.]");  // по dot
        if (atmpBarcode.length != 6) {
            atmpBarcode[0] = "";
        }
        return atmpBarcode;
    }
    private int getBarcodeQ_box(String storedbarcode) {
        String[] atmpBarcode = storedbarcode.split("[.]");  // по dot

        if (atmpBarcode.length == 6) {
            return Integer.valueOf(atmpBarcode[4]);
        }
        return 0;
    }
    private int getBarcodeN_box(String storedbarcode) {
        String[] atmpBarcode = storedbarcode.split("[.]");  // по dot
        if (atmpBarcode.length == 6) {
            return Integer.valueOf(atmpBarcode[5]);
        }
        return 0;
    }
    private String getOrder_id(String storedbarcode) {
        String so = "";
        String[] atmpBarcode = splitBarcode(storedbarcode);  // по dot

        if (StringUtils.isNotBlank(atmpBarcode[0])) {
            so = (atmpBarcode[0] + "." + atmpBarcode[1] + "." + atmpBarcode[2] + "." + atmpBarcode[3]);
        }
        return so;
    }
    private String makeOrderdef(Cursor cursor) {
        String def = "№ " + cursor.getString(2);
        def += " / " + cursor.getString(3) + "\n";
        def += "Подошва: " + cursor.getString(4) ;
        if (AppUtils.isNotEmpty(cursor.getString(5)))
            def += ", " + cursor.getString(5);
        def += "\nЗаказ: " + cursor.getString(6) +
                ". Регл: " + cursor.getString(7) +
                ". Всего кор: " + cursor.getString(8) + "\n";
        return def;
    }
    private String retStringFollowingCRIfNotNull (String s){
        String retString = "";
        try {
            if (!((s==null)||(s.equals(""))))
                retString = s+ "\n" ;
        } catch (NullPointerException e){
            Log.e(TAG, "retStringFollowingCRIfNotNull -> ".concat(e.getMessage()) );
        }
        return retString;
    }



    private static DataBaseHelper instance = null;
    /*private constructor to avoid direct instantiation by other classes*/
    private DataBaseHelper(final int DB_VERSION){
        super(AppController.getInstance().getApplicationContext(), DB_NAME, null, DB_VERSION);
    }
    private static boolean checkIfDbNeedReplace() {
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code and check if Db needs to be replaced
        int savedVersionCode = SharedPrefs.getInstance().getCodeVersion();
        boolean savedDbNeedReplace = SharedPrefs.getInstance().getDbNeedReplace();
        Log.d(TAG, "checkFirstRun -> savedDbNeedReplace -> "+savedDbNeedReplace);
        Log.d(TAG, "checkFirstRun -> currentVersionCode == savedVersionCode -> "+(currentVersionCode == savedVersionCode));
        // Check for first run or upgrade
        if (!savedDbNeedReplace & currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false;

        } else {
            Log.d(TAG, "checkFirstRun -> here new db file should be copied ");
            SharedPrefs.getInstance().setDbNeedReplace(!savedDbNeedReplace);
            SharedPrefs.getInstance().setCodeVersion(currentVersionCode);

            return true;
        }
    }
    /*synchronized method to ensure only 1 instance of LocalDBHelper exists*/
    public static synchronized DataBaseHelper getInstance(){
        if(instance == null){
            if (checkIfDbNeedReplace()) {
                instance = new DataBaseHelper(SharedPrefs.getInstance().getCodeVersion(), true);
                Log.d(TAG, "DataBaseHelper getInstance -> it was forced to replace db file");
            }else {
                instance = new DataBaseHelper(BuildConfig.VERSION_CODE, false);
                Log.d(TAG, "DataBaseHelper getInstance -> it was ordinary one");
            }
        }
        return instance;
    }
    private DataBaseHelper(final int DB_VERSION, boolean mNeedUpdate) {
        super(AppController.getInstance().getApplicationContext(), DB_NAME, null, DB_VERSION);
        DB_PATH = "/data/data/" +
                AppController.getInstance().getApplicationContext().getPackageName() +
                "/databases/";

        if (!checkDataBase()) {
            mNeedUpdate = true;
        }
        try {
            this.updateDataBase(mNeedUpdate);
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
    }

    public synchronized SQLiteDatabase openDataBase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            Log.d(TAG, "DataBaseHelper openDataBase -> incrementAndGet == 1");
            // Opening new database
            mDataBase = DataBaseHelper.getInstance().getWritableDatabase();

            selectDefsTable();
            currentOutDoc = new OutDocs(null, 0, 0,"",null,
                    defs.getDivision_code(), defs.get_idUser(), defs.get_Id_s(), defs.get_Id_d());

            return mDataBase;
        }
        return mDataBase;
    }

    public synchronized void closeDataBase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            Log.d(TAG, "DataBaseHelper openDataBase -> decrementAndGet == 0");
            // Closing database
            DataBaseHelper.getInstance().close();
        }
    }
    public static String getUUID() {
        // Creating a random UUID (Universally unique identifier).
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public void updateDataBase(boolean mNeedUpdate) throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = AppController.getInstance().getApplicationContext().getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public synchronized void close() {
        closeDataBase();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //mSharedPreferences = mContext.getSharedPreferences("WiFiBCScanerPrefsFile", Context.MODE_PRIVATE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        //db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((newVersion>oldVersion)&(oldVersion < 20))
            try {
                db.execSQL("PRAGMA foreign_keys = 0;");
                db.beginTransaction();
                //Opers
                db.execSQL("CREATE TABLE sqlitestudio_Opers_temp_table AS SELECT * FROM Opers;");
                db.execSQL("DROP TABLE Opers;");
                db.execSQL("CREATE TABLE Opers (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "Opers TEXT," +
                        "DT    INTEGER," +
                        "division_code VARCHAR (255) REFERENCES Division (code) DEFAULT (0)" +
                        ");");

                db.execSQL("INSERT INTO Opers (_id,Opers,DT,division_code)"+
                        "SELECT _id,Opers,DT,'0' FROM sqlitestudio_Opers_temp_table;");
                db.execSQL("DROP TABLE sqlitestudio_Opers_temp_table; ");

                db.execSQL("Update Opers set division_code='0' where _id =1;");
                db.execSQL("Update Opers set division_code='0' where _id =9999;");
                db.execSQL("Update Opers set division_code='00-000025' where _id =3;");
                db.execSQL("Update Opers set division_code='00-000002' where _id =2;");
                db.execSQL("Update Opers set division_code='00-000002' where _id >3 and _id<9999;");

                //Deps
                db.execSQL("CREATE TABLE sqlitestudio_temp_table AS SELECT * FROM Deps;");
                db.execSQL("DROP TABLE Deps;");
                db.execSQL("CREATE TABLE Deps (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "Id_deps TEXT," +
                        "Name_Deps TEXT," +
                        "DT    INTEGER," +
                        "division_code VARCHAR (255) REFERENCES Division (code) DEFAULT (0)," +
                        "Id_o INTEGER NOT NULL DEFAULT (0) REFERENCES Opers (_id)" +
                        ");");

                db.execSQL("INSERT INTO Deps (_id,Id_deps,Name_Deps,DT,division_code,Id_o)"+
                        "SELECT _id,Id_deps,Name_Deps,DT,'0',0 FROM sqlitestudio_temp_table;");
                db.execSQL("DROP TABLE sqlitestudio_temp_table; ");
                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
                db.execSQL("PRAGMA foreign_keys = 1;");
            }
        if ((newVersion>oldVersion)&(oldVersion < 23))
            try {
                Log.d(TAG, "Версия бд 23. Начало реструктуризации.");
                db.execSQL("PRAGMA foreign_keys = 0;");
                db.beginTransaction();

                db.execSQL("CREATE TABLE sqlitestudio_temp_table AS SELECT * FROM outDocs;");
                db.execSQL("DROP TABLE IF EXISTS outDocs;");
                db.execSQL("CREATE TABLE outDocs (_id VARCHAR (128) PRIMARY KEY UNIQUE,"+
                        "number INTEGER,"+
                        "comment VARCHAR (50)," +
                        "DT INTEGER," +
                        "Id_o INTEGER REFERENCES Opers (_id),"+
                        "sentToMasterDate INTEGER,"+
                        "division_code VARCHAR (255) REFERENCES Division (code) DEFAULT (0)," +
                        "idUser INTEGER NOT NULL REFERENCES user (_id) DEFAULT (0),"+
                        "idSotr INTEGER REFERENCES Sotr (_id),"+
                        "idDeps INTEGER REFERENCES Deps (_id));");
                db.execSQL("INSERT INTO outDocs (_id,number,comment,DT,Id_o,sentToMasterDate,division_code,idUser)"+
                        "SELECT _id,number,comment,DT,Id_o,sentToMasterDate,division_code,0 FROM sqlitestudio_temp_table;");

                db.execSQL(" update outDocs "+
                " set idSotr = (select Id_s from prods p where p.idOutDocs = outDocs._id) "+
                " where Id_o < 9999; ");

                db.execSQL(" update outDocs "+
                        " set idDeps = (select Id_d from prods p where p.idOutDocs = outDocs._id) "+
                        " where Id_o < 9999; ");

                db.execSQL("DROP TABLE sqlitestudio_temp_table;");

                db.setTransactionSuccessful();
                Log.d(TAG, "Версия бд 23. Окончание реструктуризации.");
            }
            finally {
                db.endTransaction();
                db.execSQL("PRAGMA foreign_keys = 1;");
            }
        if ((newVersion>oldVersion)&(oldVersion < 25))
            try {
                Log.d(TAG, "Версия бд 24. Начало реструктуризации.");
                db.execSQL("PRAGMA foreign_keys = 0;");
                db.beginTransaction();

                db.execSQL("CREATE TABLE sqlitestudio_temp_table AS SELECT * FROM user;");
                db.execSQL("DROP TABLE IF EXISTS user;");
                db.execSQL("CREATE TABLE user (_id INTEGER PRIMARY KEY UNIQUE,"+
                        "name VARCHAR (30) UNIQUE NOT NULL,"+
                        "pswd VARCHAR (32) NOT NULL DEFAULT (012345)," +
                        "DT INTEGER," +
                        "superUser BOOLEAN DEFAULT 0,"+
                        "expired BOOLEAN DEFAULT 0,"+
                        "Id_s INTEGER REFERENCES Sotr (_id) DEFAULT (0));");
                db.execSQL("INSERT INTO user (_id,name,pswd,DT,superUser,Id_s,expired)"+
                        "SELECT _id,name,pswd,DT,superUser,Id_s,0 FROM sqlitestudio_temp_table;");

                db.execSQL("DROP TABLE sqlitestudio_temp_table;");

                db.setTransactionSuccessful();
                Log.d(TAG, "Версия бд 24. Окончание реструктуризации.");
            }
            finally {
                db.endTransaction();
                db.execSQL("PRAGMA foreign_keys = 1;");
            }
        if ((newVersion>oldVersion)&(newVersion == 25))
            try {
                Log.d(TAG, "Версия бд 25. Начало реструктуризации.");
                db.execSQL("PRAGMA foreign_keys = 0;");
                db.beginTransaction();

                db.execSQL("CREATE TABLE sqlitestudio_temp_table AS SELECT * FROM Sotr;");
                db.execSQL("DROP TABLE IF EXISTS Sotr;");
                db.execSQL("CREATE TABLE Sotr (_id INTEGER PRIMARY KEY UNIQUE,"+
                        "tn_Sotr TEXT,"+
                        "sotr TEXT," +
                        "DT INTEGER," +
                        "Id_o INTEGER REFERENCES Opers (_id) DEFAULT (0),"+
                        "Id_d INTEGER REFERENCES Deps (_id) DEFAULT (0),"+
                        "division_code VARCHAR (255) REFERENCES Division (code) DEFAULT (0),"+
                        "expired BOOLEAN DEFAULT 0);");
                db.execSQL("INSERT INTO Sotr (_id,tn_Sotr,sotr,DT,Id_o,Id_d,division_code,expired)"+
                        "SELECT _id,tn_Sotr,sotr,DT,Id_o,Id_d,division_code,0 FROM sqlitestudio_temp_table;");

                db.execSQL("DROP TABLE sqlitestudio_temp_table;");

                db.setTransactionSuccessful();
                Log.d(TAG, "Версия бд 25. Окончание реструктуризации.");
            } catch (Exception e) {
                Log.e (TAG, e.getMessage());
            } finally {
                db.endTransaction();
                db.execSQL("PRAGMA foreign_keys = 1;");
            }
    }

    //list all boxes
    public ArrayList<HashMap<String, String>> listorders() {
        ArrayList<HashMap<String, String>> readOrders = new ArrayList<HashMap<String, String>>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
        Cursor cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord,MasterData.Q_box, " +
                "MasterData.N_box, MasterData.DT " +
                "FROM MasterData WHERE division_code=? ORDER BY MasterData._id DESC", new String [] {String.valueOf(defs.getDivision_code())});
        while (cursor.moveToNext()) {
            HashMap readOrder = new HashMap<String, String>();
            //Заполняем
            readOrder.put("Ord", cursor.getString(0) + ". " + cursor.getString(1));
            readOrder.put("Cust", cursor.getString(2) + "\n" + retStringFollowingCRIfNotNull(cursor.getString(3)) +
                    "Заказ: " + cursor.getString(4) + ". Коробок" +
                    ": " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + "\n" + " Загружен: " + lDateToString(cursor.getLong(7)));
            readOrders.add(readOrder);
        }
        tryCloseCursor(cursor);
        }catch (Exception e){
            Log.e (TAG, "listorders -> ".concat(e.getMessage()));
            HashMap readBox = new HashMap<String, String>();
            readBox.put("Ord", "Ошибка!");
            readBox.put("Cust", "Ошибка!");
            readOrders.add(readBox);
        }
        return readOrders;
    }

    public ArrayList<HashMap<String, String>> listprods() {
        ArrayList<HashMap<String, String>> readBoxes = new ArrayList<HashMap<String, String>>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            Cursor cursor = mDataBase.rawQuery("select d.Name_Deps, count(bm.Id_b), sum(RQ_box)" +
                    " from Prods p , BoxMoves bm, Deps d where bm.Id_o=" + defs.get_Id_o() + " and bm._id=p.Id_bm and p.Id_d=d._id" +
                    " and (p.sentToMasterDate is null)" +
                    " and p.p_date=(select max(p.p_date) from Prods p , BoxMoves bm where bm._id=p.Id_bm and bm.Id_o=" + defs.get_Id_o() + ")" +
                    " group by d.Name_Deps", null);
            while (cursor.moveToNext()) {
                HashMap readBox = new HashMap<String, String>();
                readBox.put("Ord", !AppUtils.isDepAndSotrOper(defs.get_Id_o()) ? defs.descOper : cursor.getString(0));
                readBox.put("Cust", "Коробок: " + cursor.getString(1) + ". Пар: " + cursor.getString(2));
                readBoxes.add(readBox);
            }
            tryCloseCursor(cursor);
        }catch (Exception e){
            Log.e (TAG, e.getMessage());
            HashMap readBox = new HashMap<String, String>();
            readBox.put("Ord", "Ошибка!");
            readBox.put("Cust", "Ошибка!");
            readBoxes.add(readBox);
        }
        return readBoxes;
    }

    public boolean updateOutDocsetSentToMasterDate (OutDocs od) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
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

    public String selectCurrentOutDocDetails (String id){
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("select p.idOutDocs, count(bm.Id_b) as boxNumber, sum(p.RQ_box) as RQ_box" +
                            " FROM Prods p, BoxMoves bm" +
                            " where p.idOutDocs='"+id+"' and bm._id=p.Id_bm"+
                            " group by p.idOutDocs", null);
            cursor.moveToNext();
            String result = ", Кор: "+cursor.getString(1)+", Под.: "+cursor.getString(2);

            return result;
        } catch (Exception e){
            return "Ошибка!";
        } finally {
            tryCloseCursor(cursor);
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
            if (checkSuperUser(defs.get_idUser())) {
                cursor = mDataBase.rawQuery("SELECT _id, number, comment, strftime('%d-%m-%Y %H:%M:%S', DT/1000, 'unixepoch', 'localtime') as DT, Id_o, division_code, idUser, idSotr, idDeps, DT as dtorder " +
                                " FROM OutDocs where _id<>0 and division_code=? and Id_o=?" +
                                " AND DT BETWEEN "+dateFrom+
                                " AND "+dateTill+
                                " ORDER BY dtorder desc, number desc",
                        new String[]{String.valueOf(defs.getDivision_code()), String.valueOf(defs.get_Id_o())});
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
                        new String[]{String.valueOf(defs.getDivision_code()), String.valueOf(defs.get_Id_o()), String.valueOf(defs.get_idUser())});
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

    //list all boxes
    public ArrayList<HashMap<String, Integer>> listboxes() {
        ArrayList<HashMap<String, Integer>> readBoxes = new ArrayList<HashMap<String, Integer>>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            if (!AppUtils.isDepAndSotrOper(defs.get_Id_o()))
                cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                        "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, MasterData.Ord_id, Boxes._id, bm._id, Prods._id" +
                        " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s Where Opers._id=" + defs.get_Id_o() +
                        " and bm.Id_o=Opers._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and bm._id=Prods.Id_bm" +
                        " and Prods.Id_d=Deps._id and Prods.Id_s=s._id and ((Prods.sentToMasterDate IS NULL) OR (Prods.sentToMasterDate=''))" +
                        " Order by MasterData.Ord_id,  Boxes.N_box", null);
            else
                cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                        "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, MasterData.Ord_id, Boxes._id, bm._id, Prods._id" +
                        " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s Where Opers._id=" + defs.get_Id_o() +
                        " and bm.Id_o=Opers._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and bm._id=Prods.Id_bm and Prods.Id_d=" + defs.get_Id_d() +
                        " and Prods.Id_d=Deps._id and Prods.Id_s=s._id and ((Prods.sentToMasterDate IS NULL) OR (Prods.sentToMasterDate=''))" +
                        " Order by MasterData.Ord_id,  Boxes.N_box", null);

            while (cursor.moveToNext()) {
                HashMap readBox = new HashMap<String, Integer>();
                String sTmp = null;
                if (!AppUtils.isDepAndSotrOper(defs.get_Id_o())) sTmp = "";
                else sTmp = cursor.getString(8) + ", " + cursor.getString(9);
                //Заполняем
                readBox.put("Ord", cursor.getString(0) + ". " + cursor.getString(1));
                readBox.put("Cust", "Подошва: " + cursor.getString(2) + ", " + retStringFollowingCRIfNotNull(cursor.getString(3))
                        + "Заказ: " + cursor.getString(4) + ". № кор: " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + " "
                        + "В кор: " + cursor.getString(7) + ". " + sTmp);
                readBox.put("bId", cursor.getString(11) + "/bId");
                readBox.put("bmId", cursor.getString(12) + "/bmId");
                readBox.put("pdId", cursor.getString(13) + "/pdId");
                //Закидываем в список
                readBoxes.add(readBox);
            }
            return readBoxes;
        }catch (Exception e) {
            Log.e(TAG, "getOutDocNotSent -> ".concat(e.getMessage()) );
            return readBoxes;
        } finally {
            tryCloseCursor(cursor);
        }
    }


    //get all Boxes  records filtered by operation
    public ArrayList<Boxes> getBoxes() {
        ArrayList<Boxes> readBoxes = new ArrayList<Boxes>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT _id,Id_m,Q_box,N_box,DT FROM Boxes where (("
                        +Boxes.COLUMN_sentToMasterDate+" IS NULL) OR ("+Boxes.COLUMN_sentToMasterDate+" = ''))", null);
            while (cursor.moveToNext()) {
                Boxes readBox = new Boxes(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), lDateToString((cursor.getLong(4))), null, false);
                if ((readBox.get_id()!= "")&(readBox.get_Id_m() != 0))
                    readBoxes.add(readBox);
            }
            return readBoxes;
        }catch (Exception e) {
            Log.e(TAG, "getBoxes -> ".concat(e.getMessage()) );
            return readBoxes;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    //get all Boxes  records filtered by operation
    public ArrayList<BoxMoves> getBoxMoves() {
        ArrayList<BoxMoves> readBoxMoves = new ArrayList<BoxMoves>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT bm._id,bm.Id_b,bm.Id_o,bm.DT FROM BoxMoves bm where ((bm."
                +BoxMoves.COLUMN_sentToMasterDate+" IS NULL) OR (bm."+BoxMoves.COLUMN_sentToMasterDate+" = ''))", null);

            while (cursor.moveToNext()) {
                readBoxMoves.add(new BoxMoves(cursor.getString(0), cursor.getString(1), cursor.getInt(2), lDateToString((cursor.getLong(3))), null));
            }
            return readBoxMoves;
        }catch (Exception e) {
            Log.e(TAG, "getBoxMoves -> ".concat(e.getMessage()) );
            return readBoxMoves;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    //get all Boxes  records filtered by operation
    public ArrayList<Prods> getProds() {
        ArrayList<Prods> readProds = new ArrayList<Prods>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try { cursor = mDataBase.rawQuery("SELECT _id, Id_bm,Id_d,Id_s,RQ_box,P_date,sentToMasterDate,idOutDocs FROM Prods where (("
                +Prods.COLUMN_sentToMasterDate+" IS NULL) OR ("+Prods.COLUMN_sentToMasterDate+" = '')) and "
                +Prods.COLUMN_Id_bm+" in " +
                "(SELECT bm._id FROM BoxMoves bm)", null);
            while (cursor.moveToNext()) {
                readProds.add(new Prods(cursor.getString(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4),
                        lDateToString(cursor.getLong(5)), cursor.getString(6), cursor.getString(7)));
            }
            return readProds;
        }catch (Exception e) {
            Log.e(TAG, "getProds -> ".concat(e.getMessage()) );
            return readProds;
        } finally {
            tryCloseCursor(cursor);
        }
    }

    public foundorder searchOrder(String storedbarcode) {
        foundorder fo = new foundorder();
        String Order_Id = getOrder_id(storedbarcode);  // по dot
        Cursor c;
        if (StringUtils.isNotBlank(Order_Id)) {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            String query = "SELECT _id,Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box,DT,archive, division_code FROM " + TABLE_MD + " WHERE Ord_id = '" + Order_Id + "'";
            c = mDataBase.rawQuery(query, null);
            try {
                c.moveToFirst();
                fo._id = c.getInt(0);
                fo.QO = c.getInt(6);
                fo.QB = c.getInt(7);
                fo.NB = c.getInt(8);
                fo.DT = lDateToString(c.getLong(9));
                fo.orderdef = makeOrderdef(c);
                fo.barcode = storedbarcode;
                fo.archive = (c.getInt(c.getColumnIndex("archive")) != 0);
                fo.division_code = c.getString(c.getColumnIndex("division_code"));
            } catch (Exception e) {
                Log.e(TAG, "searchOrder -> ".concat(e.getMessage()) );
            } finally {
                tryCloseCursor(c);
            }
        }
        return fo;
    }

    public foundbox searchBox(final int Order_id, final String storedbarcode) {
        Cursor c = null;

        foundbox fb = new foundbox();
        fb.barcode = storedbarcode;
        fb.boxdef = "";
        fb.RQ = 0;
        fb.NB = 0;
        fb._id = "";
        fb.outDocs = "";
        fb.depSotr = "";
        fb._archive = false;

        spBarcode spb = new spBarcode(storedbarcode);
        fb.QB = Integer.valueOf(spb.getQ_box());
        fb.NB = Integer.valueOf(spb.getN_box());
        fb.boxdef = "№ кор: " + spb.getN_box()+". ";
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            String query = "SELECT Boxes._id, archive FROM Boxes Where Boxes.Id_m=" + Order_id + " and Boxes.N_box=" + spb.getN_box();
            c = mDataBase.rawQuery(query, null);
            if ((c != null) & (c.getCount() != 0)) {
                c.moveToFirst(); //есть boxes & prods
                fb._id = c.getString(0);

                if (!fb._archive) {
                    if (StringUtils.isNotEmpty(fb._id)) {
                        if (!isOneOfFirstOper(defs.get_Id_o())) {//нет записи в BoxMoves и Prods. Другая операция. Определить принятое колво
                            tryCloseCursor(c);
                            query = "SELECT sum(Prods.RQ_box) as RQ_box FROM BoxMoves bm, Prods " + "" +
                                    "Where bm.Id_b='" + fb._id + "' and bm.Id_o=" + getFirstOperFor(defs.get_Id_o()) +
                                    " and bm._id=Prods.Id_bm Group by Prods.Id_bm";
                            c = mDataBase.rawQuery(query, null);
                            if ((c != null) & (c.getCount() != 0)) { //есть записи в BoxMoves и Prods для базовой операции
                                c.moveToFirst(); //есть boxes & prods
                                Log.d(TAG, "searchBox's baseOper RQ select record count = " + c.getCount() + ", _id =" + c.getString(0));
                                fb.QB = c.getInt(0);
                            } else {
                                fb.QB = 0;
                            }                          //коробка есть, по базовой операции принято 0. Ошибочная ситуация.
                            fb.boxdef += defs.descOper + ": " + fb.QB + ". ";
                        }
                        tryCloseCursor(c);
                        query = "SELECT sum(Prods.RQ_box) as RQ_box FROM Prods, BoxMoves bm " + "" +
                                "Where bm.Id_b='" + fb._id + "' and bm.Id_o=" + defs.get_Id_o() +
                                " and Prods.Id_bm=bm._id Group by Prods.Id_bm";
                        c = mDataBase.rawQuery(query, null);
                        if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                            c.moveToFirst(); //есть boxes & prods
                            Log.d(TAG, "searchBox's RQ select record count = " + c.getCount() + ", _id =" + c.getString(0));
                            fb.RQ = c.getInt(0);
                        }
                        tryCloseCursor(c);
                        query = "SELECT o.number,  strftime('%d-%m-%Y %H:%M:%S', o.DT/1000, 'unixepoch', 'localtime') as DT, Deps.Name_Deps, s.Sotr " +
                                " FROM Prods, BoxMoves bm, outDocs o, Deps, Sotr s " +
                                " Where bm.Id_b='" + fb._id + "' and bm.Id_o=" + defs.get_Id_o() +
                                " and Prods.Id_bm=bm._id  and Prods.idOutDocs=o._id and Prods.Id_d=Deps._id and Prods.Id_s=s._id order by o._id desc";
                        c = mDataBase.rawQuery(query, null);
                        if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                            c.moveToFirst(); //есть boxes & prods
                            Log.d(TAG, "Looking for outdocs record count = " + c.getCount() + ", _id =" + c.getString(0));
                            fb.outDocs = "Накл " + c.getString(0) + " от " + c.getString(1);
                            fb.depSotr = isNotEmpty(c.getString(2)) ? c.getString(2) + ", " + c.getString(3) : "";
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "searchBox -> ".concat(e.getMessage()) );
        }finally {
            fb.boxdef += "Принято: " + fb.RQ;
            tryCloseCursor(c);
        }
        return fb;
    }
    public void lastBoxCheck(foundorder fo){
        Cursor c = null;
        String query;
        try {
            try {
                mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                query = "SELECT count(b._id) as N_box FROM Boxes b, BoxMoves bm Where Id_m=" + fo._id+" and b._id=bm.Id_b and bm.Id_o="+defs.get_Id_o();
                c = mDataBase.rawQuery(query, null);
                if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                    c.moveToFirst(); //есть boxes & prods
                    if(fo.NB == c.getInt(0))
                        MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                                "Это последняя коробка этого размера из заказа!",
                                false);
                }
            } catch (SQLException e) {
                Log.e(TAG, "lastBoxCheck exception on id -> " + fo._id , e);
            }
        }finally {
            mDataBase.close();
            tryCloseCursor(c);
        }
    }

    public long insertUser(user user) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_id, user.get_id());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_Id_s, user.get_Id_s());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_name, user.getName());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_pswd, user.getPswd());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_DT, sDateTimeToLong(user.get_DT()));
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_superUser, user.isSuperUser());
            values.put(com.example.yg.wifibcscaner.data.model.user.COLUMN_EXPIRED, user.isExpired());

            return mDataBase.insertWithOnConflict(com.example.yg.wifibcscaner.data.model.user.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
    public long insertSotr(Sotr sotr) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Sotr.COLUMN_id, sotr.get_id());
            values.put(Sotr.COLUMN_Sotr, sotr.get_Sotr());
            values.put(Sotr.COLUMN_tn_Sotr, sotr.get_tn_Sotr());
            values.put(Sotr.COLUMN_DT, sDateTimeToLong(sotr.get_DT()));
            values.put(Sotr.COLUMN_Division_code, sotr.getDivision_code());
            values.put(Sotr.COLUMN_Id_d, sotr.get_Id_d());
            values.put(Sotr.COLUMN_Id_o, sotr.get_Id_o());
            values.put(Sotr.COLUMN_EXPIRED, sotr.isExpired());

            return mDataBase.insertWithOnConflict(Sotr.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
    public long insertDeps(Deps deps) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Deps.COLUMN_id, deps.get_id());
            values.put(Deps.COLUMN_Id_deps, deps.get_Id_deps());
            values.put(Deps.COLUMN_Name_Deps, deps.get_Name_Deps());
            values.put(Deps.COLUMN_DT, sDateTimeToLong(deps.get_DT()));
            values.put(Deps.COLUMN_Division_code, deps.getDivision_code());
            values.put(Deps.COLUMN_Id_o, deps.get_Id_o());

            return mDataBase.insertWithOnConflict(Deps.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    public long insertOrders(Orders orders) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Orders.COLUMN_ID, orders.get_id());
            values.put(Orders.COLUMN_Ord_Id, orders.get_Ord_Id());
            values.put(Orders.COLUMN_Ord, orders.get_Ord());
            values.put(Orders.COLUMN_Cust, orders.get_Cust());
            values.put(Orders.COLUMN_Nomen, orders.get_Nomen());
            values.put(Orders.COLUMN_Attrib, orders.get_Attrib());
            values.put(Orders.COLUMN_Q_ord, orders.get_Q_ord());
            values.put(Orders.COLUMN_Q_box, orders.get_Q_box());
            values.put(Orders.COLUMN_N_box, orders.get_N_box());
            values.put(Orders.COLUMN_DT, sDateTimeToLong(orders.get_DT()));
            values.put(Orders.COLUMN_Division_code, orders.getDivision_code());
            return (mDataBase.insertWithOnConflict(Orders.TABLE_orders, null, values, 5));
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }
    public long setLastUpdate(lastUpdate lU) {
        if (lU.getUpdateStart()==0) return 0;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(lastUpdate.COLUMN_tableName, lU.getTableName());
            values.put(lastUpdate.COLUMN_updateStart, lU.getUpdateStart());
            values.put(lastUpdate.COLUMN_updateEnd, lU.getUpdateEnd());
            values.put(lastUpdate.COLUMN_updateSuccess, lU.getUpdateSuccess());
            return mDataBase.insertWithOnConflict(lastUpdate.TABLE, null, values, 5);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    public long addOrders(foundorder fo) {
        try {
            Orders orders = new Orders(fo._id, fo.Ord_Id, fo.Ord, fo.Cust, fo.Nomen, fo.Attrib, fo.QO, fo.QB, fo.NB, fo.DT, fo.division_code, fo.archive);
            return insertOrders(orders);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public long insertOneProd(Prods prods) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Prods.COLUMN_ID, prods.get_id());
            values.put(Prods.COLUMN_Id_bm, prods.get_Id_bm());
            values.put(Prods.COLUMN_Id_d, prods.get_Id_d());
            values.put(Prods.COLUMN_Id_s, prods.get_Id_s());
            values.put(Prods.COLUMN_RQ_box, prods.get_RQ_box());
            values.put(Prods.COLUMN_P_date, sDateToLong(prods.get_P_date()));
            values.put(Prods.COLUMN_idOutDocs, prods.get_idOutDocs());
            if (prods.get_sentToMasterDate() != null) values.put(Prods.COLUMN_sentToMasterDate, sDateTimeToLong(prods.get_sentToMasterDate()));

            return mDataBase.insertWithOnConflict(Prods.TABLE_prods, null, values, 5) ;
        } catch (SQLException e) {
            Log.e(TAG, "insertOneProd exception -> ".concat(e.getMessage()));
            return 0L;
        }
    }

    public boolean addProds(foundbox fb) {
        try {
            BoxMoves bm = new BoxMoves (getUUID(),fb._id, defs.get_Id_o(),lDateToString(new Date().getTime()),null);
            if (insertBoxMoves(bm)) {
                Prods prod ;
                if (AppUtils.isDepAndSotrOper(bm.get_Id_o())) {// it needs Dep and Sotr
                    prod = new Prods(getUUID(), bm.get_id(), defs.get_Id_d(), defs.get_Id_s(), fb.RQ, DateTimeUtils.getStartOfDayString(new Date()), null,currentOutDoc.get_id());
                }
                else {
                    prod = new Prods(getUUID(), bm.get_id(), 0, 0, fb.RQ, DateTimeUtils.getStartOfDayString(new Date()), null,currentOutDoc.get_id());
                }
                return (insertOneProd(prod) > 0);
            } else return false;
        } catch (Exception e) {
            Log.e(TAG, "insertOneProd exception -> ".concat(e.getMessage()));
            return false;
        }
    }
    public boolean updateProdsSentDate(Prods prods) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Prods.COLUMN_sentToMasterDate, sDateTimeToLong(prods.get_sentToMasterDate()));
            return (mDataBase.update(Prods.TABLE_prods, values,Prods.COLUMN_ID +"='"+prods.get_id()+ "'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e(TAG, "updateProdsSentDate exception -> ".concat(e.getMessage()));
            return false;
        }
    }
    public boolean updateBoxMovesSentDate(BoxMoves bm) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();

            values.put(BoxMoves.COLUMN_sentToMasterDate, sDateTimeToLong(bm.get_sentToMasterDate()));
            return (mDataBase.update(BoxMoves.TABLE_bm, values,BoxMoves.COLUMN_ID +"='"+bm.get_id()+"'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e(TAG, "updateBoxMovesSentDate exception -> ".concat(e.getMessage()));
            return false;
        }
    }
    public boolean deleteFromTable(final String TABLE, final String COLUMN, String Value){
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            return (mDataBase.delete(TABLE, COLUMN+"='"+Value+"' and sentToMasterDate is null",null) > 0) ;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
    public boolean updateBoxesSetArchiveTrue(String bId) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();

            values.put(Boxes.COLUMN_archive, true);
            return (mDataBase.update(Boxes.TABLE_boxes, values,Boxes.COLUMN_ID +"='"+bId+"'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
    public boolean updateBoxesSentDate(Boxes boxes) {
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            ContentValues values = new ContentValues();
            values.clear();

            values.put(Boxes.COLUMN_sentToMasterDate, sDateTimeToLong(boxes.get_sentToMasterDate()));
            values.put(Boxes.COLUMN_archive, boxes.isArchive());
            return (mDataBase.update(Boxes.TABLE_boxes, values,Boxes.COLUMN_ID +"='"+boxes.get_id()+"'",null) > 0) ;
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public boolean insertBoxes(Boxes boxes) {
        Cursor cursor = null;
        try {
            try {
                mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                ContentValues values = new ContentValues();
                values.clear();
                values.put(Boxes.COLUMN_ID, boxes.get_id());
                values.put(Boxes.COLUMN_Id_m, boxes.get_Id_m());
                values.put(Boxes.COLUMN_Q_box, boxes.get_Q_box());
                values.put(Boxes.COLUMN_N_box, boxes.get_N_box());
                values.put(Boxes.COLUMN_DT, sDateTimeToLong(boxes.get_DT()));
                if (boxes.get_sentToMasterDate() != null) values.put(Boxes.COLUMN_sentToMasterDate, sDateTimeToLong(boxes.get_sentToMasterDate()));
                return (mDataBase.insertWithOnConflict(Boxes.TABLE_boxes, null, values, 5) > 0);
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, e.getMessage());
                cursor = mDataBase.rawQuery("SELECT ROWID FROM Boxes b Where b.Id_m='" + boxes.get_Id_m() +"'"+
                        " b.Q_box=" + boxes.get_Q_box() + " and b.N_box=" + boxes.get_N_box(), null);
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    cursor.moveToFirst();
                    return (cursor.getLong(0)>0);
                }
                return false;
            }
        }finally {
            tryCloseCursor(cursor);
        }
    }

    public boolean insertBoxMoves(BoxMoves bm) {
        Cursor cursor = null;
        try {
            try {
                mDataBase = AppController.getInstance().getDbHelper().openDataBase();
                cursor = mDataBase.rawQuery("SELECT bm._id as _id FROM BoxMoves bm Where bm.Id_o=" + bm.get_Id_o() + " and bm.Id_b='" + bm.get_Id_b()+"'", null);
                if ((cursor != null) && (cursor.getCount() > 0)) {
                    try {
                        cursor.moveToFirst();
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
            }
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public boolean addBoxes(foundorder fo, int iRQ) {
        try {
            foundbox fb = new foundbox();
            fb._id = "";
            String sDate = DateTimeUtils.getDayTimeString(new Date());
            Boxes boxes = new Boxes(getUUID(), fo._id, getBarcodeQ_box(fo.barcode), getBarcodeN_box(fo.barcode), sDate, null, false);
             //Дату DT пишем прямо в insertBoxes
            if (insertBoxes(boxes)) {
                fb._id = boxes.get_id();
                fb.RQ = iRQ;
                return addProds(fb);
            }
            return false;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return false;
        }
    }

    public String lastBox() {
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        String product = "Инфо о принятых коробках нет.";
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(p.ROWID) as _id FROM Boxes, BoxMoves bm, Prods p, OutDocs o Where Boxes._id=bm.Id_b and bm.Id_o=" + defs.get_Id_o() +
                    " and bm._id=p.Id_bm and p.idOutDocs=o._id and o.division_code=?", new String[]{String.valueOf(defs.getDivision_code())});
            if ((cursor != null) && (cursor.moveToFirst())) {
                Log.d(TAG, "lastbox Records count = " + cursor.getCount());

                try {
                    cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                            "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, o.number,  strftime('%d-%m-%Y %H:%M:%S', o.DT/1000, 'unixepoch', 'localtime') as DT" +
                            " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s, outDocs o Where MasterData.division_code=?" +
                            " and Opers._id=" + defs.get_Id_o() + " and Prods.ROWID=" + cursor.getLong(0) +
                            " and Opers._id=bm.Id_o and Prods.Id_bm=bm._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and Prods.Id_d=Deps._id" +
                            " and Prods.Id_s=s._id  and Prods.idOutDocs=o._id" +
                            " Order by Prods._id desc", new String[]{String.valueOf(defs.getDivision_code())});
                    try {
                        if ((cursor != null) & (cursor.getCount() > 0)) {
                            cursor.moveToFirst();
                            product = "№ " + cursor.getString(0);
                            product += " / " + cursor.getString(1) + "\n";
                            product += "Подошва: " + cursor.getString(2);
                            if (AppUtils.isNotEmpty(cursor.getString(3)))
                                product += ", " + cursor.getString(3);

                            product += "\nЗаказ: " + cursor.getString(4) + ". № кор: " + cursor.getString(6) +
                                    ". Регл: " + cursor.getString(5) + " ";
                            product += "В кор: " + cursor.getString(7) + "." + "\n";
                            product += isNotEmpty(cursor.getString(8)) ? cursor.getString(8) + ", " + cursor.getString(9) + "\n" : ""; //Бригада
                            product += "Накл " + cursor.getString(10) + " от " + cursor.getString(11);
                        }
                    } catch (CursorIndexOutOfBoundsException e) {
                        Log.e(TAG, "lastBox CursorIndexOutOfBoundsException -> ".concat(e.getMessage()));
                        return product;
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "lastBox SQLException rawQuery -> ".concat(e.getMessage()));
                    return product;
                }
            }
            return product;
        } finally {
            tryCloseCursor(cursor);
        }
    }

    public List<String> getAllUserName() {
        ArrayList<String> alUserName = new ArrayList<String>();
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();

            cursor = mDataBase.rawQuery("SELECT name FROM user WHERE _id<>0 and NOT expired order by name", null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    alUserName.add(cursor.getString(0));
                }
            }
            return alUserName;
        }catch (Exception e) {
            Log.e(TAG, "getAllUserName -> ".concat(e.getMessage()));
            return alUserName;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public int getUserIdByName(String nm) {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id FROM user Where name='" + nm + "'", null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }catch (Exception e) {
            Log.e(TAG, "getUserIdByName -> ".concat(e.getMessage()));
            return 0;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public Boolean checkUserPswdById(int id, String pswd){
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT _id FROM user Where _id=? and pswd=?",
                    new String [] {String.valueOf(id), String.valueOf(pswd)});
            if ((cursor != null) && cursor.moveToFirst()) {
                return cursor.getInt(0) != 0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkUserPswdById -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public boolean checkSuperUser (int _id) {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT superUser FROM user Where _id=?",
                    new String [] {String.valueOf(_id)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) != 0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkSuperUser -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public boolean checkIfUserTableEmpty () {
        Cursor cursor = null;
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            cursor = mDataBase.rawQuery("SELECT count(*) FROM user Where _id<>0",
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0)==0;
            }
            return false;
        }catch (Exception e) {
            Log.e(TAG, "checkIfUserTableEmpty -> ".concat(e.getMessage()));
            return false;
        } finally {
            tryCloseCursor(cursor);
        }
    }



    public long updateDefsTable(Defs defs){
        try {
            long l;
            mDataBase = this.getWritableDatabase();
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
            l = mDataBase.update(Defs.table_Defs, values,strFilter, null);
            mDataBase.close();
            return l;
        } catch (SQLException e) {
            return 0;
        }
    }
    public int selectDefsTable(){
        try {
            mDataBase = this.getReadableDatabase();
            Cursor cursor = mDataBase.rawQuery("SELECT Id_d,Id_o,Id_s,Host_IP,Port,idOperFirst,idOperLast,division_code,idUser,DeviceId  FROM Defs ", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                defs = new Defs(cursor.getInt(0),cursor.getInt(1),cursor.getInt(2),
                        cursor.getString(3),cursor.getString(4),cursor.getInt(5),
                        cursor.getInt(6),cursor.getString(7),cursor.getInt(8),cursor.getString(9));
                defs.descOper = operRepo.getOperNameById(defs.get_Id_o());
                defs.descDep = depRepo.getDepNameById(defs.get_Id_d());
                defs.descSotr = getSotr_Name_by_id(defs.get_Id_s());
                defs.descDivision = divRepo.getDivisionNameByCode(defs.getDivision_code());
                defs.descUser = getUserName(defs.get_idUser());
                defs.descFirstOperForCurrent = operRepo.getOperNameById(getFirstOperFor(defs.get_Id_o()));
            }
            tryCloseCursor(cursor);
            mDataBase.close();
            return 1;
        } catch (SQLException e) {
            return 0;
        }
    }

    public String getUserName(int code){
        mDataBase = this.getReadableDatabase();
        String nm = "";
        Cursor cursor = mDataBase.rawQuery("SELECT name FROM user Where _id=?", new String [] {String.valueOf(code)});
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            nm = String.format("%s", cursor.getString(0));
        }
        tryCloseCursor(cursor);
        mDataBase.close();
        return nm;
    }
    public int getUserId_s(int _id){
        mDataBase = this.getReadableDatabase();
        int nm = 0;
        Cursor cursor = mDataBase.rawQuery("SELECT Id_s FROM user Where _id=?", new String [] {String.valueOf(_id)});
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            nm = cursor.getInt(0);
        }
        tryCloseCursor(cursor);
        mDataBase.close();
        return nm;
    }
    public List<Sotr> getSotrIdByDep(String division_code, int operation_id, int department_id) {
        ArrayList<Sotr> sotrArrayList = new ArrayList<Sotr>();
        mDataBase = this.getReadableDatabase();
        try ( Cursor cursor = mDataBase.rawQuery("SELECT _id, Sotr FROM Sotr " +
                        "Where division_code=? and Id_o=? and Id_d=? Order by _id",
                new String [] {String.valueOf(division_code), String.valueOf(operation_id), String.valueOf(department_id)}) ) {
            while (cursor.moveToNext()) {
                sotrArrayList.add(new Sotr(cursor.getInt(0), cursor.getString(1)) );
            }
            tryCloseCursor(cursor);
        }
        mDataBase.close();
        return sotrArrayList;
    }
    public List<String> getAllSotrName(String code, int department_id, int operation_id) {
        ArrayList<String> nameDeps = new ArrayList<String>();
        mDataBase = this.getReadableDatabase();
        try ( Cursor cursor = mDataBase.rawQuery("SELECT _id,tn_Sotr,Sotr FROM Sotr " +
                "Where NOT expired and (((division_code=?) and (Id_o=?) and (Id_d=?))) or (_id=0) Order by _id",
                new String [] {String.valueOf(code), String.valueOf(operation_id), String.valueOf(department_id)}) ) {
            while (cursor.moveToNext()) {
                nameDeps.add(String.format("%s, %s", cursor.getString(2), cursor.getString(1)));
            }
            tryCloseCursor(cursor);
        }
        return nameDeps;
    }
    public Sotr getSotrReq(int Id_s){
        mDataBase = this.getReadableDatabase();
        Sotr sotr = new Sotr(Id_s, "0",0,0);
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT Id_o, Id_d, division_code FROM Sotr Where _id=?", new String [] {String.valueOf(Id_s)});
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                sotr.set_Id_o(cursor.getInt(0));
                sotr.set_Id_d(cursor.getInt(1));
                sotr.setDivision_code(cursor.getString(2));
            }
            tryCloseCursor(cursor);
            mDataBase.close();
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return sotr;
        }
    }
    public int getSotr_id_by_Name(String nm){
        mDataBase = this.getReadableDatabase();
        int num = 0;
        if( nm.indexOf(", ") > 0) {
            nm = substring(nm, nm.indexOf("0"), nm.length());
            Cursor cursor = mDataBase.rawQuery("SELECT _id FROM Sotr Where tn_Sotr='" + nm + "'", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                num = cursor.getInt(0);
            }
            tryCloseCursor(cursor);
            mDataBase.close();
        }
        return num;
    }
    public String getSotr_Name_by_id(int iD){
        mDataBase = this.getReadableDatabase();
        String nm = "";
        Cursor cursor = mDataBase.rawQuery("SELECT tn_Sotr, Sotr FROM Sotr Where _id="+ iD, null);
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            nm = String.format("%s %s", cursor.getString(1), cursor.getString(0));
        }
        tryCloseCursor(cursor);
        mDataBase.close();
        return nm;
    }

    public String getMaxOrderDate(){
        String nm = dtMin;
        if (!globalUpdateDate.equals("")) {nm = globalUpdateDate; return nm;}

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT updateStart, updateEnd, updateSuccess FROM lastUpdate WHERE tableName=?", new String [] {Orders.TABLE_orders});
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                //nm = lDateToString(cursor.getLong(0));
                if(!cursor.isNull(0)) nm = DateTimeUtils.getStartOfDayString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }

    public String getMaxUserDate(){
        String nm = dtMin;
        if (!globalUpdateDate.equals("")) {nm = globalUpdateDate;
            return nm;}

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM user", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = lDateToString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }
    public String getMaxSotrDate(){
        String nm = dtMin;
        if (!globalUpdateDate.equals("")) {nm = globalUpdateDate;
        return nm;}

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Sotr", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = lDateToString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }

    public String getMaxOpersDate(){
        String nm = dtMin;
        if (!globalUpdateDate.equals("")) {nm = globalUpdateDate; return nm;}

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT max(DT) FROM Opers", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = lDateToString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }

    public int getId_dByOutdoc(String idOutDocs){
        int result = 0;
        mDataBase = this.getReadableDatabase();
        Cursor cursor = mDataBase.rawQuery("SELECT distinct(Id_d) FROM Prods Where idOutDocs='" + idOutDocs + "'", null);
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getInt(0);
                cursor.moveToNext();
            }
        }
        tryCloseCursor(cursor);
        mDataBase.close();
        return result;
    }
    public String getProdsMinDate(){
        String nm = dtMin;

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT min(P_date) FROM Prods", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = lDateToString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }
    public Long getProdsMinDateLong(){
        Long nm = ldtMin;

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT min(P_date) FROM Prods", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = DateTimeUtils.getLongStartOfDayLong(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }
    public String getTableMinDate(String tableName){
        String nm = dtMin;

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT min(DT) FROM "+tableName, null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = lDateToString(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }
    public Long getTableMinDateLong(String tableName){
        Long nm = ldtMin;

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT min(DT) FROM "+tableName, null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                nm = DateTimeUtils.getLongStartOfDayLong(cursor.getLong(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return nm;
        }
    }
    public String getTableRecordsCount(String tableName){
        String count = "0";

        Cursor cursor = null;
        try {
            mDataBase = this.getReadableDatabase();
            cursor = mDataBase.rawQuery("SELECT COUNT(*) FROM "+tableName, null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                count = String.valueOf(cursor.getInt(0));
            }
        } finally {
            tryCloseCursor(cursor);
            mDataBase.close();
            return count;
        }
    }
    public int getId_sByOutDocId(String idOutDocs){
        int result = 0;
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        Cursor cursor = mDataBase.rawQuery("SELECT distinct(Id_s) FROM Prods Where idOutDocs='" + idOutDocs + "'", null);
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getInt(0);
                cursor.moveToNext();
            }
        }
        tryCloseCursor(cursor);
        ////mDataBase.close();
        return result;
    }
    //Insert in Bulk
    public void insertOrdersInBulk(List<Orders> list){
        if (!mDataBase.isOpen()) {
            mDataBase = this.getWritableDatabase();
        }
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
    public void insertOutDocInBulk(List<OutDocs> list){
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        try {

            //mDataBase.execSQL("PRAGMA foreign_keys = 0;");
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
    public void insertBoxInBulk(List<Boxes> list){
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        try {

            //mDataBase.execSQL("PRAGMA foreign_keys = 0;");
            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO Boxes (_id, Id_m, Q_box, N_box, DT, sentToMasterDate, archive) " +
                    " VALUES (?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Boxes o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindLong(2, o.get_Id_m());
                statement.bindLong(3, o.get_Q_box());
                statement.bindLong(4, o.get_N_box());
                statement.bindLong(5, getDateTimeLong(o.get_DT()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(6, new Date().getTime());
                else
                    statement.bindLong(6, getDateTimeLong(o.get_sentToMasterDate()));

                statement.bindLong(7, (o.isArchive() ? 1 : 0));
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

    public void insertBoxMoveInBulk(List<BoxMoves> list) {
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        try {

            //mDataBase.execSQL("PRAGMA foreign_keys = 0;");
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

            mDataBase.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
            //mDataBase.execSQL("PRAGMA foreign_keys = 1;");
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new RuntimeException("To catch into upper level.");
        } finally {
            mDataBase.endTransaction();
        }
    }

    public void insertProdInBulk(List<Prods> list) {
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        try {

            //mDataBase.execSQL("PRAGMA foreign_keys = 0;");
            mDataBase.beginTransaction();
            String sql = "INSERT OR REPLACE INTO Prods (_id, Id_bm, Id_d, Id_s, RQ_box, P_date, sentToMasterDate, idOutDocs) " +
                    " VALUES (?,?,?,?,?,?,?,?);";

            SQLiteStatement statement = mDataBase.compileStatement(sql);

            for (Prods o : list) {
                statement.clearBindings();
                statement.bindString(1, o.get_id());
                statement.bindString(2, o.get_Id_bm());
                statement.bindLong(3, o.get_Id_d());
                statement.bindLong(4, o.get_Id_s());
                statement.bindLong(5, o.get_RQ_box());
                statement.bindLong(6, getDateLong(o.get_P_date()));

                if (o.get_sentToMasterDate() == null)
                    statement.bindLong(7, new Date().getTime());
                else
                    statement.bindLong(7, getDateTimeLong(o.get_sentToMasterDate()));
                statement.bindString(8, o.get_idOutDocs());
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



    public int getOneSotrIdByDepId(int depId){
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        Cursor cursor = null;
        int result = 0;
        try {
                cursor = mDataBase.rawQuery("SELECT _id FROM Sotr WHERE Id_d=? LIMIT 1", new String [] {String.valueOf(depId)});
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    result = cursor.getInt(0);
                }
        } catch (Exception e) {
            Log.w(TAG, e);
        } finally {
            cursor.close();
            return result;
        }
    }
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

        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
        Cursor cursor = null;

        try {
            if (checkSuperUser(defs.get_idUser())) {
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
            sotrId = getOneSotrIdByDepId(depId);
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
        if (!mDataBase.isOpen()) {
            mDataBase = this.getReadableDatabase();
        }
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String saveToDB(OrderOutDocBoxMovePart r) {
        try {
            insertOrdersInBulk(r.orderReqList);

            if (r.outDocReqList != null &&
                    !r.outDocReqList.isEmpty())
                insertOutDocInBulk(r.outDocReqList);

            if (r.boxReqList != null &&
                    !r.boxReqList.isEmpty())
                insertBoxInBulk(r.boxReqList);

            if (r.movesReqList != null &&
                    !r.movesReqList.isEmpty())
                insertBoxMoveInBulk(r.movesReqList);

            if (r.partBoxReqList != null &&
                    !r.partBoxReqList.isEmpty())
                insertProdInBulk(r.partBoxReqList);

            Log.d(TAG, "saveToDB reached its return point.");
            return Collections.max(r.orderReqList, Comparator.comparing(Orders::get_DT)).get_DT();
        } catch (RuntimeException re) {
            Log.w(TAG, re);
            throw new RuntimeException("To catch onto method level.");
        }
    }
}

