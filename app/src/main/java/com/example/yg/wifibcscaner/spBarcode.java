package com.example.yg.wifibcscaner;

/**
 * Created by yg on 16.12.2017.
 */

public class spBarcode {
    private String spbarcode;
    private String Ord_id;
    private String Attrib;
    private String Obraz;
    private String Q_ord;
    private String Q_box;
    private String N_box;

    public spBarcode(String barcode){
        String atmpBarcode[] = barcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 6);
        if (!b) {
            atmpBarcode[0]="";
        }else{
            this.N_box = atmpBarcode[5];
            this.spbarcode=barcode;
            this.Ord_id =atmpBarcode[0];
            this.Attrib=atmpBarcode[1];
            this.Obraz=atmpBarcode[2];
            this.Q_ord=atmpBarcode[3];
            this.Q_box=atmpBarcode[4];
        }
    }

    public String getSpbarcode() {
        return spbarcode;
    }

    public void setSpbarcode(String spbarcode) {
        this.spbarcode = spbarcode;
    }

    public String getOrd_id() {
        return Ord_id;
    }

    public void setOrd_id(String ord_id) {
        Ord_id = ord_id;
    }

    public String getAttrib() {
        return Attrib;
    }

    public void setAttrib(String attrib) {
        Attrib = attrib;
    }

    public String getObraz() {
        return Obraz;
    }

    public void setObraz(String obraz) {
        Obraz = obraz;
    }

    public String getQ_ord() {
        return Q_ord;
    }

    public void setQ_ord(String q_ord) {
        Q_ord = q_ord;
    }

    public String getQ_box() {
        return Q_box;
    }

    public void setQ_box(String q_box) {
        Q_box = q_box;
    }

    public String getN_box() {
        return N_box;
    }

    public void setN_box(String n_box) {
        N_box = n_box;
    }
}
