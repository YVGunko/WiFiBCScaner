package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 15.12.2017.
 */

public class ApiUtils {
    public static PartBoxService getBoxesService(String baseUrl) {
        return RetrofitClient.getClient(baseUrl).create(PartBoxService.class);
    }

    public static OrderService getOrderService(String baseUrl) {
        return RetrofitClient.getClient(baseUrl).create(OrderService.class);
    }

}

