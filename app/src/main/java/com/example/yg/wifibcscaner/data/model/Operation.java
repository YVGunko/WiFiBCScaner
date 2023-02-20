package com.example.yg.wifibcscaner.data.model;

/**
 * Created by yg on 29.01.2018.
 */

public class Operation {
    public static final String TABLE = "Opers";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_Opers = "Opers";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_Division = "division_code";

    private int _id;
    private String _Opers;
    private String _dt;
    private String division_code;

    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    public Operation(int _id, String _Opers, String _dt, String division_code) {
        this._id = _id;
        this._Opers = _Opers;
        this._dt = _dt;
        this.division_code = division_code;
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
