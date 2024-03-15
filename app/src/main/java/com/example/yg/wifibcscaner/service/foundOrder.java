package com.example.yg.wifibcscaner.service;

public class foundOrder {
    private String barcode; //строка описания
    private String orderdef; //строка описания
    private int _id; //
    private String Ord_Id;
    private String Ord;
    private String Cust;
    private String Nomen;
    private String Attrib;
    private int QO; //количество в заказе
    private int QB; //количество в коробке
    private int NB; //Количество коробок
    private String DT;
    private String division_code;
    private boolean archive;

    public foundOrder(String barcode, String orderdef, int _id, String ord_Id, String ord, String cust, String nomen, String attrib, int QO, int QB, int NB, String DT, String division_code, boolean archive) {
        this.barcode = barcode;
        this.orderdef = orderdef;
        this._id = _id;
        this.Ord_Id = ord_Id;
        this.Ord = ord;
        this.Cust = cust;
        this.Nomen = nomen;
        this.Attrib = attrib;
        this.QO = QO;
        this.QB = QB;
        this.NB = NB;
        this.DT = DT;
        this.division_code = division_code;
        this.archive = archive;
    }
    public foundOrder() {
        this.barcode = "";
        this.orderdef = "";
        this._id = 0;
        this.Ord_Id = "";
        this.Ord = "";
        this.Cust = "";
        this.Nomen = "";
        this.Attrib = "";
        this.division_code = "";
    }
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getOrderdef() {
        return orderdef;
    }

    public void setOrderdef(String orderdef) {
        this.orderdef = orderdef;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOrd_Id() {
        return Ord_Id;
    }

    public void setOrd_Id(String ord_Id) {
        Ord_Id = ord_Id;
    }

    public String getOrd() {
        return Ord;
    }

    public void setOrd(String ord) {
        Ord = ord;
    }

    public String getCust() {
        return Cust;
    }

    public void setCust(String cust) {
        Cust = cust;
    }

    public String getNomen() {
        return Nomen;
    }

    public void setNomen(String nomen) {
        Nomen = nomen;
    }

    public String getAttrib() {
        return Attrib;
    }

    public void setAttrib(String attrib) {
        Attrib = attrib;
    }

    public int getQO() {
        return QO;
    }

    public void setQO(int QO) {
        this.QO = QO;
    }

    public int getQB() {
        return QB;
    }

    public void setQB(int QB) {
        this.QB = QB;
    }

    public int getNB() {
        return NB;
    }

    public void setNB(int NB) {
        this.NB = NB;
    }

    public String getDT() {
        return DT;
    }

    public void setDT(String DT) {
        this.DT = DT;
    }

    public String getDivision_code() {
        return division_code;
    }

    public void setDivision_code(String division_code) {
        this.division_code = division_code;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}
