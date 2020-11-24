package com.example.yg.wifibcscaner;

public class Sotr {
    private int _id;
    private String _tn_Sotr;
    private String _Sotr;
    private String _DT;
    private String division_code;
    private int Id_o;
    private int Id_d;

    public static final String COLUMN_Id_d = "Id_d";
    public static final String COLUMN_Id_o = "Id_o";
    public static final String TABLE = "Sotr";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_Sotr = "Sotr";
    public static final String COLUMN_tn_Sotr = "tn_Sotr";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_Division_code = "division_code";

    public Sotr(int _id, String _tn_Sotr, String _Sotr, String _DT, String division_code, int Id_d, int Id_o) {
        this._id = _id;
        this._tn_Sotr = _tn_Sotr;
        this._Sotr = _Sotr;
        this._DT = _DT;
        this.division_code = division_code;
        this.Id_d = Id_d;
        this.Id_o = Id_o;
    }

    public Sotr(int _id, String division_code, int Id_d, int Id_o) {
        this._id = _id;
        this.division_code = division_code;
        this.Id_d = Id_d;
        this.Id_o = Id_o;
    }

    public int get_Id_o() {
        return Id_o;
    }

    public void set_Id_o(int Id_o) {
        this.Id_o = Id_o;
    }

    public int get_Id_d() {
        return Id_d;
    }

    public void set_Id_d(int Id_d) {
        this.Id_d = Id_d;
    }

    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_tn_Sotr() {
        return _tn_Sotr;
    }

    public void set_tn_Sotr(String _tn_Sotr) {
        this._tn_Sotr = _tn_Sotr;
    }

    public String get_Sotr() {
        return _Sotr;
    }

    public void set_Sotr(String _Sotr) {
        this._Sotr = _Sotr;
    }

    public String get_DT() {
        return _DT;
    }

    public void set_DT(String _DT) {
        this._DT = _DT;
    }
}
