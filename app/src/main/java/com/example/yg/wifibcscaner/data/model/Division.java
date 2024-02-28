package com.example.yg.wifibcscaner.data.model;


public class Division {
    public static final String TABLE = "Division";
    public static final String COLUMN_Name = "name";
    public static final String COLUMN_Code = "code";

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Division(String code, String name) {
        this.name = name;
        this.code = code;
    }

}
