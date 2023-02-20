package com.example.yg.wifibcscaner.data.model;

import java.util.Date;

/**
 * Created by yg on 18.12.2017.
 */

public class Orders {
    private int _id;
    private String _Ord_Id;
    private String _Ord;
    private String _Cust;
    private String _Nomen;
    private String _Attrib;
    private int _Q_ord;
    private int _Q_box;
    private int _N_box;
    private String _DT;
    private Boolean archive;

    public Orders() {
    }

    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    private String division_code;

    public static final String TABLE_orders = "MasterData";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Ord_Id = "Ord_Id";
    public static final String COLUMN_Ord = "Ord";
    public static final String COLUMN_Cust = "Cust";
    public static final String COLUMN_Nomen = "Nomen";
    public static final String COLUMN_Attrib = "Attrib";
    public static final String COLUMN_Q_ord = "Q_ord";
    public static final String COLUMN_Q_box = "Q_box";
    public static final String COLUMN_N_box = "N_box";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_Division_code = "division_code";
    public static final String COLUMN_Archive = "archive";

 /*   public Orders(int _id,String _Ord_Id,String _Ord,String _Cust,String _Nomen,String _Attrib, int _Q_ord, int _Q_box, int _N_box, String _DT, String division_code) {
        this._id = _id;
        this._Ord_Id = _Ord_Id;
        this._Ord = _Ord;
        this._Cust = _Cust;
        this._Nomen = _Nomen;
        this._Attrib = _Attrib;
        this._Q_ord = _Q_ord;
        this._Q_box = _Q_box;
        this._N_box = _N_box;
        this._DT = _DT;
        this.division_code = division_code;
    }*/

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Orders(int _id, String _Ord_Id, String _Ord, String _Cust, String _Nomen, String _Attrib, int _Q_ord, int _Q_box, int _N_box, String _DT, String division_code, Boolean archive) {
        this._id = _id;
        this._Ord_Id = _Ord_Id;
        this._Ord = _Ord;
        this._Cust = _Cust;
        this._Nomen = _Nomen;
        this._Attrib = _Attrib;
        this._Q_ord = _Q_ord;
        this._Q_box = _Q_box;
        this._N_box = _N_box;
        this._DT = _DT;
        this.division_code = division_code;
        this.archive = archive;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_Ord_Id() {
        return _Ord_Id;
    }

    public void set_Ord_Id(String _Ord_Id) {
        this._Ord_Id = _Ord_Id;
    }

    public String get_Ord() {
        return _Ord;
    }

    public void set_Ord(String _Ord) {
        this._Ord = _Ord;
    }

    public String get_Cust() {
        return _Cust;
    }

    public void set_Cust(String _Cust) {
        this._Cust = _Cust;
    }

    public String get_Nomen() {
        return _Nomen;
    }

    public void set_Nomen(String _Nomen) {
        this._Nomen = _Nomen;
    }

    public String get_Attrib() {
        return _Attrib;
    }

    public void set_Attrib(String _Attrib) {
        this._Attrib = _Attrib;
    }

    public int get_Q_ord() {
        return _Q_ord;
    }

    public void set_Q_ord(int _Q_ord) {
        this._Q_ord = _Q_ord;
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

    public String get_DT() {
        return _DT;
    }

    public void set_DT(String _DT) {
        this._DT = _DT;
    }
}
