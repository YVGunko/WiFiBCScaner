package com.example.yg.wifibcscaner;

/**
 * Created by yg on 17.04.2018.
 */

public class OutDocs {
    private String _id;
    private int _number;
    private String _comment;
    private String _DT;
    private int _Id_o;
    private String _sentToMasterDate;
    private String division_code;
    private  String DeviceId;
    private int idUser;

    public static final String TABLE = "OutDocs";
    public static final String COLUMN_Id = "_id";
    public static final String COLUMN_number = "number";
    public static final String COLUMN_comment = "comment";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_Id_o = "Id_o";
    public static final String COLUMN_idUser = "idUser";
    public static final String COLUMN_Division_code = "division_code";


    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public OutDocs(String _id, int _Id_o, int _number, String _comment, String _DT, String _sentToMasterDate, String division_code, int idUser ) {
        this._id = _id;
        this._number = _number;
        this._comment = _comment;
        this._DT = _DT;
        this._Id_o = _Id_o;
        this.idUser = idUser;
        this._sentToMasterDate = _sentToMasterDate;
        this.division_code = division_code;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int get_Id_o() {
        return _Id_o;
    }

    public void set_Id_o(int _Id_o) {
        this._Id_o = _Id_o;
    }

    public int get_number() {
        return _number;
    }

    public void set_number(int _number) {
        this._number = _number;
    }

    public String get_comment() {
        return _comment;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }

    public String get_DT() {
        return _DT;
    }

    public void set_DT(String _DT) {
        this._DT = _DT;
    }

    public void set_sentToMasterDate(String _sentToMasterDate) {
        this._sentToMasterDate = _sentToMasterDate;
    }
    public String get_sentToMasterDate() {
        return _sentToMasterDate;
    }
}
