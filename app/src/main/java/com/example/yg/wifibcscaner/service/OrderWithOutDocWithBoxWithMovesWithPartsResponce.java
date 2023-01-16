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
    public List<OutDocs> outDocsList = new ArrayList<>();
    public List<Boxes> boxReqList = new ArrayList<>();
    public List<BoxMoves> movesReqList = new ArrayList<>();
    public List<Prods> partBoxReqList = new ArrayList<>();

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
}
