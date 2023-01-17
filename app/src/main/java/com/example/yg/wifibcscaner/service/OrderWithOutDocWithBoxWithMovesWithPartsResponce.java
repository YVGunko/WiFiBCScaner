package com.example.yg.wifibcscaner.service;

import com.example.yg.wifibcscaner.BoxMoves;
import com.example.yg.wifibcscaner.Boxes;
import com.example.yg.wifibcscaner.Orders;
import com.example.yg.wifibcscaner.OutDocs;
import com.example.yg.wifibcscaner.Prods;

import java.util.ArrayList;
import java.util.List;

public class OrderWithOutDocWithBoxWithMovesWithPartsResponce {
    private Orders order = new Orders();
    private List<OutDocs> outDocsList = new ArrayList<>();
    private List<Boxes> boxReqList = new ArrayList<>();
    private List<BoxMoves> movesReqList = new ArrayList<>();
    private List<Prods> partBoxReqList = new ArrayList<>();

    public OrderWithOutDocWithBoxWithMovesWithPartsResponce(Orders order,
                                                            List<OutDocs> outDocsList,
                                                            List<Boxes> boxReqList,
                                                            List<BoxMoves> movesReqList,
                                                            List<Prods> partBoxReqList) {
        this.order = order;
        this.outDocsList = outDocsList;
        this.boxReqList = boxReqList;
        this.movesReqList = movesReqList;
        this.partBoxReqList = partBoxReqList;
    }

    public Orders getOrder() {
        return order;
    }

    public List<OutDocs> getOutDocsList() {
        return outDocsList;
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
