package com.example.yg.wifibcscaner.data.dto;

import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OutDocWithBoxWithMovesWithPartsRequest {
    @NotNull
    public List<OutDocs> outDocReqList = new ArrayList<>();
    @NotNull
    public List<Boxes> boxReqList = new ArrayList<>();
    @NotNull
    public List<Prods> partBoxReqList = new ArrayList<>();
    @NotNull
    public List<BoxMoves> movesReqList = new ArrayList<>();

    public OutDocWithBoxWithMovesWithPartsRequest(@NotNull List<OutDocs> outDocReqList,
                                                  @NotNull List<Boxes> boxReqList,
                                                  @NotNull List<BoxMoves> movesReqList,
                                                  @NotNull List<Prods> partBoxReqList) {
        this.outDocReqList = outDocReqList;
        this.boxReqList = boxReqList;
        this.movesReqList = movesReqList;
        this.partBoxReqList = partBoxReqList;
    }

    public OutDocWithBoxWithMovesWithPartsRequest() {

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
