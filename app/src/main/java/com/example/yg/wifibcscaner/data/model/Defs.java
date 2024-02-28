package com.example.yg.wifibcscaner.data.model;

/**
 * Created by yg on 14.12.2017.
 * CREATE TABLE IF NOT EXISTS `Defs` (
 `_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
 `Host_IP`	TEXT,
 `Port`	TEXT,
 `Id_d`	INTEGER,
 `Id_o`	INTEGER,
 FOREIGN KEY(`Id_d`) REFERENCES `Deps`(`_id`),
 FOREIGN KEY(`Id_o`) REFERENCES `Opers`(`_id`)
 );
 */

public class Defs {
    public static final String table_Defs = "Defs";
    public static final String COLUMN_Id_d = "Id_d";
    public static final String COLUMN_Id_s = "Id_s";
    public static final String COLUMN_Id_o = "Id_o";
    public static final String COLUMN_idUser = "idUser";
    public static final String COLUMN_DeviceId = "deviceId";
    public static final String COLUMN_Host_IP = "Host_IP";
    public static final String COLUMN_Port = "Port";
    public static final String COLUMN_idOperFirst = "idOperFirst";
    public static final String COLUMN_idOperLast = "idOperLast";
    public static final String COLUMN_Division_code = "division_code";
    public String descOper = "Производство";
    public String descDivision = "";
    public String descDep = "";
    public String descSotr = "";
    public String descUser = "";
    public String descFirstOperForCurrent = "";
    private String _Host_IP;
    private String _Port;

    private String division_code;
    private String DeviceId;
    private int _Id_d;
    private int _Id_o;
    private int _Id_s;
    private int _idOperFirst;
    private int _idOperLast;
    private int _idUser;

    public Defs(int _Id_d, int _Id_o, int _Id_s, String _Host_IP, String _Port, int _idOperFirst, int _idOperLast, String Division_code, int _idUser, String DeviceId) {
        this._Id_d = _Id_d;
        this._Id_o = _Id_o;
        this._Id_s = _Id_s;
        this._Host_IP = _Host_IP;
        this._Port = _Port;
        this._idOperFirst = _idOperFirst;
        this._idOperLast = _idOperLast;
        this.division_code = Division_code;
        this.DeviceId = DeviceId;
        this._idUser = _idUser;
    }
    public Defs(int _Id_d, int _Id_o, int _Id_s, String _Host_IP, String _Port, String Division_code, int _idUser, String DeviceId) {
        this._Id_d = _Id_d;
        this._Id_o = _Id_o;
        this._Id_s = _Id_s;
        this._Host_IP = _Host_IP;
        this._Port = _Port;
        this.division_code = Division_code;
        this.DeviceId = DeviceId;
        this._idUser = _idUser;
    }
    public Defs(int _Id_d, int _Id_o, int _Id_s, String _Host_IP, String _Port, String Division_code, String DeviceId) {
        this._Id_d = _Id_d;
        this._Id_o = _Id_o;
        this._Id_s = _Id_s;
        this._Host_IP = _Host_IP;
        this._Port = _Port;
        this.division_code = Division_code;
        this.DeviceId = DeviceId;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String DeviceId) {
        this.DeviceId = DeviceId;
    }

    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    public int get_idUser() {
        return _idUser;
    }

    public void set_idUser(int _idUser) {
        this._idUser = _idUser;
    }

    public int get_idOperFirst() {
        return _idOperFirst;
    }

    public void set_idOperFirst(int _idOperFirst) {
        this._idOperFirst = _idOperFirst;
    }

    public int get_idOperLast() {
        return _idOperLast;
    }

    public void set_idOperLast(int _idOperLast) {
        this._idOperLast = _idOperLast;
    }

    public int get_Id_d() {
        return _Id_d;
    }

    public void set_Id_d(int _Id_d) {
        this._Id_d = _Id_d;
    }

    public int get_Id_s() {
        return _Id_s;
    }

    public void set_Id_s(int _Id_s) {
        this._Id_s = _Id_s;
    }

    public int get_Id_o() {
        return _Id_o;
    }

    public void set_Id_o(int _Id_o) {
        this._Id_o = _Id_o;
    }

    public String get_Host_IP() {
        return _Host_IP;
    }

    public void set_Host_IP(String _Host_IP) {
        this._Host_IP = _Host_IP;
    }

    public String get_Port() {
        return _Port;
    }

    public void set_Port(String _Port) {
        this._Port = _Port;
    }

    public String getUrl() {return "http://" + this._Host_IP+":"+this._Port;}

}
