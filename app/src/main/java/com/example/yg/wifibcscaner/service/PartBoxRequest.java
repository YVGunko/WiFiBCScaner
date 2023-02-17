package com.example.yg.wifibcscaner.service;

import com.example.yg.wifibcscaner.BoxMoves;
import com.example.yg.wifibcscaner.data.repository.Boxes;
import com.example.yg.wifibcscaner.Prods;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yg on 20.12.2017.
 */

public class PartBoxRequest {
    public PartBoxRequest(List<Boxes> boxReqList, List<BoxMoves> movesReqList, List<Prods> partBoxReqList) {
        this.boxReqList = boxReqList;
        this.partBoxReqList = partBoxReqList;
        this.movesReqList = movesReqList;
    }
    public List<Boxes> boxReqList = new ArrayList<>();
    public List<BoxMoves> movesReqList = new ArrayList<>();
    public List<Prods> partBoxReqList = new ArrayList<>();
}
