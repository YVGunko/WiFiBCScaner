package com.example.yg.wifibcscaner.data.dto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OutDocWithBoxWithMovesWithPartsIdOnlyRequest {
    @NotNull
    public List<String> outDocIdList = new ArrayList<>();

    public List<String> getBoxIdList() {
        return boxIdList;
    }

    public void setBoxIdList(List<String> boxIdList) {
        this.boxIdList = boxIdList;
    }

    public List<String> getPartBoxIdList() {
        return partBoxIdList;
    }

    public void setPartBoxIdList(List<String> partBoxIdList) {
        this.partBoxIdList = partBoxIdList;
    }

    public List<String> getBoxMoveIdList() {
        return boxMoveIdList;
    }

    public void setBoxMoveIdList(List<String> boxMoveIdList) {
        this.boxMoveIdList = boxMoveIdList;
    }

    @NotNull
    public List<String> boxIdList = new ArrayList<>();
    @NotNull
    public List<String> partBoxIdList = new ArrayList<>();
    @NotNull
    public List<String> boxMoveIdList = new ArrayList<>();

    public List<String> getOutDocIdList() {
        return outDocIdList;
    }

    public void setOutDocIdList(List<String> outDocIdList) {
        this.outDocIdList = outDocIdList;
    }
}
