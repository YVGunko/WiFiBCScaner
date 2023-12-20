package com.example.yg.wifibcscaner.data;

public class user {
    public static final String TABLE = "user";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_name = "name";
    public static final String COLUMN_pswd = "pswd";
    public static final String COLUMN_superUser = "superUser";
    public static final String COLUMN_Id_s = "Id_s";
    public static final String COLUMN_DT = "DT";
    public static final String COLUMN_EXPIRED = "expired";
    private int _id;
    private String name;
    private String pswd;
    private boolean superUser;
    private boolean expired;
    private int Id_s;
    private String DT;

    public user(int id, String name, String pswd, boolean superUser, int Id_s, String DT, boolean expired ) {
        _id = id;
        this.name = name;
        this.pswd = pswd;
        this.superUser = superUser;
        this.expired = expired;
        this.Id_s = Id_s;
        this.DT = DT;
    }

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    public boolean isSuperUser() {
        return superUser;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    public int get_Id_s() {
        return Id_s;
    }

    public void set_Id_s(int Id_s) {
        this.Id_s = Id_s;
    }

    public String get_DT() {
        return DT;
    }

    public void set_DT(String DT) {
        this.DT = DT;
    }


    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
