package com.example.yg.wifibcscaner.data.dto;

import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;

import java.util.ArrayList;
import java.util.List;

public class OutDocRequest {
    public OutDocRequest(List<OutDocs> outDocsList, List<Boxes> boxReqList, List<BoxMoves> movesReqList, List<Prods> partBoxReqList) {
        this.outDocsList = outDocsList;
        this.boxReqList = boxReqList;
        this.partBoxReqList = partBoxReqList;
        this.movesReqList = movesReqList;
    }
    public List<OutDocs> outDocsList = new ArrayList<>();
    public List<Boxes> boxReqList = new ArrayList<>();
    public List<BoxMoves> movesReqList = new ArrayList<>();
    public List<Prods> partBoxReqList = new ArrayList<>();
}
