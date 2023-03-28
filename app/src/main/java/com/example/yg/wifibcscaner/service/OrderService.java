package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 23.01.2018.
 */


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.example.yg.wifibcscaner.data.dto.OrderWithOutDocWithBoxWithMovesWithPartsResponce;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.model.user;

public interface OrderService {
    @GET("/dataPageable/v1")
    Call<OrderOutDocBoxMovePart> getDataPageableV1(@Query("date") String date,
                                             @Query("division_code") String division_code,
                                             @Query("page") int page);
    @GET("/order/{id}")
    Call<OrderWithOutDocWithBoxWithMovesWithPartsResponce> getOrder(@Path("id") String orderId);
    @GET("/order/v35")
    Call<List<Orders>> getOrdersByDivision(@Query("date") String date, @Query("division_code") String division_code);
    @GET("/order/v36")
    Call<List<String>> getOrdersId(@Query("date") String date, @Query("division_code") String division_code);

    @GET("/order")
    Call<List<Orders>> getOrders(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);
    @GET("/boxesByDate")
    Call<List<Boxes>> getBoxesByDate(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);
    @GET("/bmByDateAndArchive")
    Call<List<BoxMoves>> getBoxMovesByDate(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);
    @GET("/pbByDateAndArchive")
    Call<List<Prods>> getPartBoxByDate(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);

    @GET("/bmByDatePageble")
    Call<List<BoxMoves>> getBoxMovesByDatePageble(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId, @Query("page") int page);
    @GET("/bmByDatePagebleCount")
    Call<Integer> getBoxMovesByDatePagebleCount(@Query("date") String date);
    @GET("/pbByDatePageble")
    Call<List<Prods>> getPartBoxByDatePageble(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId, @Query("page") int page);
    @GET("/pbByDatePagebleCount")
    Call<Integer> getPartBoxByDatePagebleCount(@Query("date") String date);

    @GET("/division")
    Call<List<Division>> getDivision(@Query("division_code") String division_code);

    @GET("/division")
    Call<List<Division>> getDiv();

    @GET("/employee")
    Call<List<Sotr>> getSotr(@Query("date") String date);

    @GET("/user")
    Call<List<user>> getUser(@Query("date") String date);

    @GET("/department")
    Call<List<Deps>> getDeps(@Query("date") String date);

    @GET("/operation")
    Call<List<Operation>> getOperation(@Query("date") String date);

    @POST("/outDocSaveOrUpdate")
    Call<List<OutDocs>> addOutDoc(@Body ArrayList<OutDocs> outDocs, @Query("deviceId") String deviceId);

    @POST("/outDocPost")
    Call<List<OutDocs>> getOutDocPost(@Body ArrayList<String> odId);

    @GET("/outDocGet")
    Call<List<OutDocs>> getOutDocGet(@Query("date") String date);

    @GET("/serverUpdateTime")
    Call<Long> getServerUpdateTime();

    @GET("/")
    Call<Object> checkConnection();
}