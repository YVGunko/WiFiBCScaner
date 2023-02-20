package com.example.yg.wifibcscaner.data.model;

/**
 * Created by yg on 19.12.2017.
 */

public class Deps {
    public static final String TABLE = "Deps";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_Name_Deps = "Name_Deps";
    public static final String COLUMN_Id_deps = "Id_deps";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_Division_code = "division_code";
    public static final String COLUMN_Id_o = "Id_o";
    private int _id;
    private String _Id_deps;
    private String _Name_Deps;
    private String _DT;
    private String division_code;
    private int Id_o;


    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        division_code = division_code;
    }

    public Deps(int _id, String _Id_deps, String _Name_Deps, String _DT, String Division_code, int Id_o) {
        this._id = _id;
        this._Id_deps = _Id_deps;
        this._Name_Deps = _Name_Deps;
        this._DT = _DT;
        this.division_code = Division_code;
        this.Id_o = Id_o;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_Id_deps() {
        return _Id_deps;
    }

    public void set_Id_deps(String _Id_deps) {
        this._Id_deps = _Id_deps;
    }

    public String get_Name_Deps() {
        return _Name_Deps;
    }

    public void set_Name_Deps(String _Name_Deps) {
        this._Name_Deps = _Name_Deps;
    }
    public String get_DT() {
        return _DT;
    }

    public void set_DT(String _DT) {
        this._DT = _DT;
    }

    public int get_Id_o() {
        return Id_o;
    }

    public void set_Id_o(int Id_o) {
        this.Id_o = Id_o;
    }
}
