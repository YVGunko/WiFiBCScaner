package com.example.yg.wifibcscaner;

/**
 * Created by yg on 12.12.2017.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.model.lastUpdate;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.service.foundOrder;
import com.example.yg.wifibcscaner.service.spBarcode;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.yg.wifibcscaner.utils.AppUtils.getFirstOperFor;
import static com.example.yg.wifibcscaner.utils.AppUtils.isNotEmpty;
import static com.example.yg.wifibcscaner.utils.AppUtils.isOneOfFirstOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.tryCloseCursor;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.lDateToString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateTimeToLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateToLong;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.getUUID;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.makeOrderdef;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper";

    private static String DB_PATH = "";
    private static String DB_NAME = "SQR.db";


    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";
    public long serverUpdateTime;

    private SQLiteDatabase mDataBase;
    private AtomicInteger mOpenCounter = new AtomicInteger(0);

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
        // don't know why it initially contains -1;
        if (mOpenCounter.compareAndSet(-1, 0));
        if (mOpenCounter.incrementAndGet() == 1) {
            Log.d(TAG, "DataBaseHelper openDataBase -> incrementAndGet == 1");
            // Opening new database
            mDataBase = DataBaseHelper.getInstance().getWritableDatabase();
            return mDataBase;
        }
        Log.i(TAG, "DataBaseHelper mOpenCounter = ".concat(mOpenCounter.toString()));
        return mDataBase;
    }

    public synchronized void closeDataBase() {
        Log.i(TAG, "DataBaseHelper mOpenCounter = ".concat(mOpenCounter.toString()));
        if(mOpenCounter.decrementAndGet() == 0) {
            Log.d(TAG, "DataBaseHelper closeDataBase -> decrementAndGet == 0");
            // Closing database
            DataBaseHelper.getInstance().close();
        }
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


    public ArrayList<HashMap<String, String>> listprods() {
        ArrayList<HashMap<String, String>> readBoxes = new ArrayList<HashMap<String, String>>();
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        try {
            Cursor cursor = mDataBase.rawQuery("select d.Name_Deps, count(bm.Id_b), sum(RQ_box)" +
                    " from Prods p , BoxMoves bm, Deps d where bm.Id_o=" + AppController.getInstance().getDefs().get_Id_o() + " and bm._id=p.Id_bm and p.Id_d=d._id" +
                    " and (p.sentToMasterDate is null)" +
                    " and p.p_date=(select max(p.p_date) from Prods p , BoxMoves bm where bm._id=p.Id_bm and bm.Id_o=" + AppController.getInstance().getDefs().get_Id_o() + ")" +
                    " group by d.Name_Deps", null);
            while (cursor.moveToNext()) {
                HashMap readBox = new HashMap<String, String>();
                readBox.put("Ord", !AppUtils.isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o()) ? AppController.getInstance().getDefs().getDescOper() : cursor.getString(0));
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



    public foundBox searchBox(final int Order_id, final String storedbarcode) {
        Cursor c = null;
        spBarcode spb = new spBarcode(storedbarcode);
        foundBox fb = new foundBox(storedbarcode, "№ кор: " + spb.getN_box()+". ", Integer.valueOf(spb.getQ_box()), Integer.valueOf(spb.getN_box()));

        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            String query = "SELECT Boxes._id, archive FROM Boxes Where Boxes.Id_m=" + Order_id + " and Boxes.N_box=" + spb.getN_box();
            c = mDataBase.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                fb.set_id (c.getString(0));
                fb.set_archive (c.getInt(c.getColumnIndex("archive")) != 0);

                if (!fb.is_archive()) {
                    if (StringUtils.isNotEmpty(fb.get_id())) {
                        if (!isOneOfFirstOper(AppController.getInstance().getDefs().get_Id_o())) {//нет записи в BoxMoves и Prods. Другая операция. Определить принятое колво
                            tryCloseCursor(c);
                            query = "SELECT sum(Prods.RQ_box) as RQ_box FROM BoxMoves bm, Prods " + "" +
                                    "Where bm.Id_b='" + fb.get_id() + "' and bm.Id_o=" + getFirstOperFor(AppController.getInstance().getDefs().get_Id_o()) +
                                    " and bm._id=Prods.Id_bm Group by Prods.Id_bm";
                            c = mDataBase.rawQuery(query, null);
                            if ((c != null) & (c.getCount() != 0)) { //есть записи в BoxMoves и Prods для базовой операции
                                c.moveToFirst(); //есть boxes & prods
                                Log.d(TAG, "searchBox's baseOper RQ select record count = " + c.getCount() + ", _id =" + c.getString(0));
                                fb.setQB( c.getInt(0) );
                            } else {
                                fb.setQB( 0 );
                            }                          //коробка есть, по базовой операции принято 0. Ошибочная ситуация.
                            fb.setBoxdef(fb.getBoxdef().concat(AppController.getInstance().getDefs().getDescOper()+ ": " + fb.getQB() + ". "));
                        }
                        tryCloseCursor(c);
                        query = "SELECT sum(Prods.RQ_box) as RQ_box FROM Prods, BoxMoves bm " + "" +
                                "Where bm.Id_b='" + fb.get_id() + "' and bm.Id_o=" + AppController.getInstance().getDefs().get_Id_o() +
                                " and Prods.Id_bm=bm._id Group by Prods.Id_bm";
                        c = mDataBase.rawQuery(query, null);
                        if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                            c.moveToFirst(); //есть boxes & prods
                            Log.d(TAG, "searchBox's RQ select record count = " + c.getCount() + ", _id =" + c.getString(0));
                            fb.setRQ( c.getInt(0) );
                        }
                        tryCloseCursor(c);
                        query = "SELECT o.number,  strftime('%d-%m-%Y %H:%M:%S', o.DT/1000, 'unixepoch', 'localtime') as DT, Deps.Name_Deps, s.Sotr " +
                                " FROM Prods, BoxMoves bm, outDocs o, Deps, Sotr s " +
                                " Where bm.Id_b='" + fb.get_id() + "' and bm.Id_o=" + AppController.getInstance().getDefs().get_Id_o() +
                                " and Prods.Id_bm=bm._id  and Prods.idOutDocs=o._id and Prods.Id_d=Deps._id and Prods.Id_s=s._id order by o._id desc";
                        c = mDataBase.rawQuery(query, null);
                        if ((c != null) & (c.getCount() != 0)) {            //есть записи в BoxMoves и Prods
                            c.moveToFirst(); //есть boxes & prods
                            Log.d(TAG, "Looking for outdocs record count = " + c.getCount() + ", _id =" + c.getString(0));
                            fb.setOutDocs( "Накл " + c.getString(0) + " от " + c.getString(1));
                            fb.setDepSotr( isNotEmpty(c.getString(2)) ? c.getString(2) + ", " + c.getString(3) : "" );
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "searchBox -> ".concat(e.getMessage()) );
        }finally {
            fb.setBoxdef(fb.getBoxdef().concat("Принято: " + fb.getRQ()));
            tryCloseCursor(c);
        }
        return fb;
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

    public boolean deleteFromTable(final String TABLE, final String COLUMN, String Value){
        try {
            mDataBase = AppController.getInstance().getDbHelper().openDataBase();
            return (mDataBase.delete(TABLE, COLUMN+"='"+Value+"' and sentToMasterDate is null",null) > 0) ;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public String lastBox() {
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        String product = "Инфо о принятых коробках нет.";
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT max(p.ROWID) as _id FROM Boxes, BoxMoves bm, Prods p, OutDocs o Where Boxes._id=bm.Id_b and bm.Id_o=" + AppController.getInstance().getDefs().get_Id_o() +
                    " and bm._id=p.Id_bm and p.idOutDocs=o._id and o.division_code=?", new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code())});
            if ((cursor != null) && (cursor.moveToFirst())) {
                Log.d(TAG, "lastbox Records count = " + cursor.getCount());

                try {
                    cursor = mDataBase.rawQuery("SELECT MasterData.Ord, MasterData.Cust, MasterData.Nomen, MasterData.Attrib, MasterData.Q_ord, " +
                            "Boxes.Q_box, Boxes.N_box, Prods.RQ_box, Deps.Name_Deps, s.Sotr, o.number,  strftime('%d-%m-%Y %H:%M:%S', o.DT/1000, 'unixepoch', 'localtime') as DT" +
                            " FROM Opers, Boxes, BoxMoves bm, Prods, Deps, MasterData, Sotr s, outDocs o Where MasterData.division_code=?" +
                            " and Opers._id=" + AppController.getInstance().getDefs().get_Id_o() + " and Prods.ROWID=" + cursor.getLong(0) +
                            " and Opers._id=bm.Id_o and Prods.Id_bm=bm._id and Boxes._id=bm.Id_b and Boxes.Id_m=MasterData._id and Prods.Id_d=Deps._id" +
                            " and Prods.Id_s=s._id  and Prods.idOutDocs=o._id" +
                            " Order by Prods._id desc", new String[]{String.valueOf(AppController.getInstance().getDefs().getDivision_code())});
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

    public String getTableMinDate(String tableName){
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT min(DT), max(DT) FROM "+tableName, null);
            if (cursor != null && cursor.moveToFirst()){
                return lDateToString(cursor.getLong(0)).concat(" - ").concat(lDateToString(cursor.getLong(1)));
            }
            return DateTimeUtils.getDayTimeString(new Date());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return DateTimeUtils.getDayTimeString(new Date());
        } finally {
            tryCloseCursor(cursor);
        }
    }
    public String getTableRecordsCount(String tableName){
        mDataBase = AppController.getInstance().getDbHelper().openDataBase();
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT COUNT(*) FROM "+tableName, null);
            if (cursor != null && cursor.moveToFirst()){
                return String.valueOf(cursor.getInt(0));
            }
            return "Ошибка!";
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return "Ошибка!";
        } finally {
            tryCloseCursor(cursor);
        }
    }









}

