package com.example.yg.wifibcscaner.data.dto;

import com.example.yg.wifibcscaner.data.BoxMoves;
import com.example.yg.wifibcscaner.data.Boxes;
import com.example.yg.wifibcscaner.data.Orders;
import com.example.yg.wifibcscaner.data.OutDocs;
import com.example.yg.wifibcscaner.data.Prods;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrderOutDocBoxMovePart {
    @NotNull
    public List<Orders> orderReqList = new ArrayList<>();
    @NotNull
    public List<OutDocs> outDocReqList = new ArrayList<>();
    @NotNull
    public List<Boxes> boxReqList = new ArrayList<>();
    @NotNull
    public List<Prods> partBoxReqList = new ArrayList<>();
    @NotNull
    public List<BoxMoves> movesReqList = new ArrayList<>();
}
