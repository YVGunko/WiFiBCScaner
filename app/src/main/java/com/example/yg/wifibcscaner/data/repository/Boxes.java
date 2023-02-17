package com.example.yg.wifibcscaner.data.repository;

/**
 CREATE TABLE Boxes (
 _id   INTEGER  PRIMARY KEY AUTOINCREMENT,
 Id_m  INTEGER,
 Q_box INTEGER,
 N_box INTEGER,
 DT    DATETIME NOT NULL
 DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (
 Id_m
 )
 REFERENCES MasterData (_id)
 );
 */

public class Boxes {
    private String _id;
    private int _Id_m;
    private int _Q_box;
    private int _N_box;
    private String _DT;
    private String _sentToMasterDate;
    private boolean _archive;

    public static final String TABLE_boxes = "Boxes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Id_m = "Id_m";
    public static final String COLUMN_Q_box = "Q_box";
    public static final String COLUMN_N_box = "N_box";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";
    public static final String COLUMN_archive = "archive";


    public boolean isArchive() {
        return _archive;
    }

    public void setArchive(boolean archive) {
        this._archive = _archive;
    }

    public Boxes(String _id, int _Id_m, int _Q_box, int _N_box, String _DT, String _sentToMasterDate, boolean _archive)  {
        this._id = _id;
        this._Id_m = _Id_m;
        this._Q_box = _Q_box;
        this._N_box = _N_box;
        this._DT = _DT;
        this._sentToMasterDate = _sentToMasterDate;
        this._archive = _archive;
    }

    public int get_Id_m() {
        return _Id_m;
    }

    public void set_Id_m(int _Id_m) {
        this._Id_m = _Id_m;
    }

    public int get_Q_box() {
        return _Q_box;
    }

    public void set_Q_box(int _Q_box) {
        this._Q_box = _Q_box;
    }

    public int get_N_box() {
        return _N_box;
    }

    public void set_N_box(int _N_box) {
        this._N_box = _N_box;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_DT() {       return _DT;    }

    public void set_DT(String _DT) {
        this._DT = _DT;
    }

    public String get_sentToMasterDate() {
        return _sentToMasterDate;
    }

    public void set_sentToMasterDate(String _sentToMasterDate) {
        this._sentToMasterDate = _sentToMasterDate;
    }
}
