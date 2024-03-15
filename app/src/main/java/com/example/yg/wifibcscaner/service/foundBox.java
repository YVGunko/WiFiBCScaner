package com.example.yg.wifibcscaner.service;

public class foundBox {
    private String barcode; //строка описания
    private String boxdef; //строка описания
    private int QB; //количество в коробке
    private int NB; //# коробa
    private int RQ; //принятое количество всего в коробке
    private String _id;
    private String outDocs;
    private String depSotr;
    private boolean _archive;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBoxdef() {
        return boxdef;
    }

    public void setBoxdef(String boxdef) {
        this.boxdef = boxdef;
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

    public int getRQ() {
        return RQ;
    }

    public void setRQ(int RQ) {
        this.RQ = RQ;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getOutDocs() {
        return outDocs;
    }

    public void setOutDocs(String outDocs) {
        this.outDocs = outDocs;
    }

    public String getDepSotr() {
        return depSotr;
    }

    public void setDepSotr(String depSotr) {
        this.depSotr = depSotr;
    }

    public boolean is_archive() {
        return _archive;
    }

    public void set_archive(boolean _archive) {
        this._archive = _archive;
    }

    public foundBox(String barcode, String boxdef, int QB, int NB, int RQ, String _id, String outDocs, String depSotr, boolean _archive) {
        this.barcode = barcode;
        this.boxdef = boxdef;
        this.QB = QB;
        this.NB = NB;
        this.RQ = RQ;
        this._id = _id;
        this.outDocs = outDocs;
        this.depSotr = depSotr;
        this._archive = _archive;
    }
    public foundBox(String barcode, String boxdef, int QB, int NB) {
        this.barcode = barcode;
        this.boxdef = boxdef;
        this.QB = QB;
        this.NB = NB;
        this.RQ = 0;
        this._id = "";
        this.outDocs = "";
        this.depSotr = "";
        this._archive = false;
    }
    public foundBox() {
        this.barcode = "";
        this.boxdef = "";
        this.QB = 0;
        this.NB = 0;
        this.RQ = 0;
        this._id = "";
        this.outDocs = "";
        this.depSotr = "";
        this._archive = false;
    }
}
