package com.example.yg.wifibcscaner.service;

import com.example.yg.wifibcscaner.Orders;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yg on 23.01.2018.
 */

public class OrderRequest {
    public OrderRequest(List<Orders> orderReqList) {
        this.orderReqList = orderReqList;
    }

    private List<Orders> orderReqList = new ArrayList<>();
}
