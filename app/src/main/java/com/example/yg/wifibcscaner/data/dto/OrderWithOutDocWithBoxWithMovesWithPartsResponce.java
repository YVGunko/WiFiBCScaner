package com.example.yg.wifibcscaner.data.dto;

import com.example.yg.wifibcscaner.data.BoxMoves;
import com.example.yg.wifibcscaner.data.Boxes;
import com.example.yg.wifibcscaner.data.Orders;
import com.example.yg.wifibcscaner.data.OutDocs;
import com.example.yg.wifibcscaner.data.Prods;

import java.util.ArrayList;
import java.util.List;

public class OrderWithOutDocWithBoxWithMovesWithPartsResponce {
    private Orders orderReq = new Orders();
    private List<OutDocs> outDocReqList = new ArrayList<>();
    private List<Boxes> boxReqList = new ArrayList<>();
    private List<BoxMoves> movesReqList = new ArrayList<>();
    private List<Prods> partBoxReqList = new ArrayList<>();

    public OrderWithOutDocWithBoxWithMovesWithPartsResponce(Orders orderReq,
                                                            List<OutDocs> outDocReqList,
                                                            List<Boxes> boxReqList,
                                                            List<BoxMoves> movesReqList,
                                                            List<Prods> partBoxReqList) {
        this.orderReq = orderReq;
        this.outDocReqList = outDocReqList;
        this.boxReqList = boxReqList;
        this.movesReqList = movesReqList;
        this.partBoxReqList = partBoxReqList;
    }

    public Orders getOrder() {
        return orderReq;
    }

    public List<OutDocs> getOutDocReqList() {
        return outDocReqList;
    }

    public List<Boxes> getBoxReqList() {
        return boxReqList;
    }

    public List<BoxMoves> getMovesReqList() {
        return movesReqList;
    }

    public List<Prods> getPartBoxReqList() {
        return partBoxReqList;
    }
}
