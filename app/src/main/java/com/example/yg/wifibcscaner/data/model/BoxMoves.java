package com.example.yg.wifibcscaner.data.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yg on 11.01.2018.
 CREATE TABLE `BoxMoves` (
 `_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
 `Id_b`	INTEGER,
 `Id_o`	INTEGER,
 `DT`	DateTime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(`Id_b`) REFERENCES `Boxes`(`_id`),
 FOREIGN KEY(`Id_o`) REFERENCES `Opers`(`_id`)
 );
 */

public class BoxMoves {
    private String _id;
    private String _Id_b;
    private int _Id_o;
    private String _DT;
    private String _sentToMasterDate;
    public static final String TABLE_bm = "BoxMoves";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Id_b = "Id_b";
    public static final String COLUMN_Id_o = "Id_o";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";

    public BoxMoves(String _id, String _Id_b, int _Id_o, String _DT, String _sentToMasterDate)  {
        this._id = _id;
        this._Id_b = _Id_b;
        this._Id_o = _Id_o;
        this._DT = _DT;
        this._sentToMasterDate = _sentToMasterDate;
    }

    public String get_DT() {return _DT;}

    public void set_DT(String _DT) {
        this._DT = _DT;
    }

    public String get_sentToMasterDate() {
        return _sentToMasterDate;
    }

    public void set_sentToMasterDate(String _sentToMasterDate) {
        this._sentToMasterDate = _sentToMasterDate;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_Id_b() {
        return _Id_b;
    }

    public void set_Id_b(String _Id_b) {
        this._Id_b = _Id_b;
    }

    public int get_Id_o() {
        return _Id_o;
    }

    public void set_Id_o(int _Id_o) {
        this._Id_o = _Id_o;
    }

}
