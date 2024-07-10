package com.example.yg.wifibcscaner.data.model;

public class BoxSizing {
    // Table Name
    public static final String TABLE_NAME = "BOX_SIZING";
    // Table columns
    public static final String _ID = "_id";
    public static final String ORDER = "order_id";
    public static final String QUANTITY = "quantity";
    public static final String SIZE = "size";

    private int id;
    private int masterDataId;
    private int quantity;
    private String size;

    // Creating table query
    public static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ORDER + " INTEGER NOT NULL REFERENCES "+Orders.TABLE_NAME+", "
            + QUANTITY + " INTEGER NOT NULL, "
            + SIZE + " VARCHAR (50) NOT NULL );";
    public  static final String SQL_INSERT_REPLACE = "INSERT OR REPLACE INTO " + TABLE_NAME + "("
            + ORDER +", " + QUANTITY + ", " + SIZE + ") "
            + " VALUES (?,?,?);";
    public  static final String SQL_SELECT_BOX_SIZING_FOR_MD_ID_IN = "SELECT "+ ORDER + ", " + QUANTITY
            + " FROM " + TABLE_NAME
            + " WHERE " + ORDER + " IN ( ? ) ; ";
    public BoxSizing(int id, int masterDataId, int quantity, String size) {
        this.id = id;
        this.masterDataId = masterDataId;
        this.quantity = quantity;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMasterDataId() {
        return masterDataId;
    }

    public void setMasterDataId(int masterDataId) {
        this.masterDataId = masterDataId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
