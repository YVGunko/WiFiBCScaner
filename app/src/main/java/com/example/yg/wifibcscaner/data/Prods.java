package com.example.yg.wifibcscaner.data;

/**
 * Created by yg on 14.12.2017.
 CREATE TABLE Prods (
 _id    INTEGER PRIMARY KEY AUTOINCREMENT,
 Id_bm  INTEGER REFERENCES BoxMoves (_id),
 Id_d   INTEGER REFERENCES Deps (_id),
 Id_s   INTEGER REFERENCES Sotr (_id),
 RQ_box INTEGER,
 P_date DATE    NOT NULL
 );
 */

public class Prods {

    private String _id;
    private int _Id_d;
    private String _Id_bm;
    private int _Id_s;
    private int _RQ_box;
    private String _P_date;
    private String _sentToMasterDate;
    private String _idOutDocs;
    public static final String TABLE_prods = "Prods";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Id_d = "Id_d";
    public static final String COLUMN_Id_bm = "Id_bm";
    public static final String COLUMN_Id_s = "Id_s";
    public static final String COLUMN_RQ_box = "RQ_box";
    public static final String COLUMN_P_date = "P_date";
    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";
    public static final String COLUMN_idOutDocs = "idOutDocs";

    public Prods(String _id, String _Id_bm, int _Id_d, int _Id_s, int _RQ_box, String _P_date, String _sentToMasterDate, String _idOutDocs)  {

        this._id = _id;
        this._Id_bm = _Id_bm;
        this._Id_d = _Id_d;
        this._Id_s = _Id_s;
        this._RQ_box = _RQ_box;
        this._P_date = _P_date;
        this._sentToMasterDate = _sentToMasterDate;
        this._idOutDocs = _idOutDocs;
    }

    public int get_Id_s() {
        return _Id_s;
    }
    public void set_Id_s() {
        this._Id_s = _Id_s;
    }

    public int get_Id_d() {
        return _Id_d;
    }
    public void set_Id_d(int _Id_d) {
        this._Id_d = _Id_d;
    }

    public String get_Id_bm() {
        return _Id_bm;
    }
    public void set_Id_bm(String _Id_bm) {
        this._Id_bm = _Id_bm;
    }

    public int get_RQ_box() {
        return _RQ_box;
    }
    public void set_RQ_box(int _RQ_box) {
        this._RQ_box = _RQ_box;
    }

    public void set_P_date(String _P_date) {
        this._P_date = _P_date;
    }
    public String get_P_date() {
        return _P_date;
    }

    public void set_sentToMasterDate(String _sentToMasterDate) {
        this._sentToMasterDate = _sentToMasterDate;
    }
    public String get_sentToMasterDate() {
        return _sentToMasterDate;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_idOutDocs() {
        return _idOutDocs;
    }

    public void set_idOutDocs(String _idOutDocs) {
        this._idOutDocs = _idOutDocs;
    }
}