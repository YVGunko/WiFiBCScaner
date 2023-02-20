package com.example.yg.wifibcscaner.data.model;

public class OrderNotFound {
    public static final String TABLE = "orderNotFound";
    private String orderId;

    public OrderNotFound(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
