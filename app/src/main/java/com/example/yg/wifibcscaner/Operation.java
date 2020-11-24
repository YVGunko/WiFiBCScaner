package com.example.yg.wifibcscaner;

/**
 * Created by yg on 29.01.2018.
 */

public class Operation {
    public static final String TABLE = "Opers";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_Opers = "Opers";
    public static final String COLUMN_DT = "DT";

    private int _id;
    private String _Opers;
    private String _dt;

    public Operation(int _id, String _Opers, String _dt) {
        this._id = _id;
        this._Opers = _Opers;
        this._dt = _dt;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_Opers() {
        return _Opers;
    }

    public void set_Opers(String _Opers) {
        this._Opers = _Opers;
    }

    public String get_dt() {
        return _dt;
    }

    public void set_dt(String _dt) {
        this._dt = _dt;
    }
}
