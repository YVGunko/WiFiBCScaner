package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 23.01.2018.
 */


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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderService {

    @GET("/dataPageable/v1")
    Call<OrderOutDocBoxMovePart> getDataPageableV1(@Query("date") String date,
                                               @Query("division_code") String division_code,
                                               @Query("operationId") long operationId,
                                               @Query("page") int pageNumber,
                                               @Query("pageSize") int pageSize);

    @POST("/partBox")
    Call<PartBoxRequest> addBoxes(@Body PartBoxRequest partBoxRequest, @Query("userId") int userId, @Query("deviceId") String deviceId);

    @GET("/order")
    Call<List<Orders>> getOrders(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);
    @GET("/boxesByDate")
    Call<List<Boxes>> getBoxesByDate(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId);

    @GET("/bmByDatePageble")
    Call<List<BoxMoves>> getBoxMovesByDatePageble(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId, @Query("page") int page);
    @GET("/bmByDatePagebleCount")
    Call<Integer> getBoxMovesByDatePagebleCount(@Query("date") String date);
    @GET("/pbByDatePageble")
    Call<List<Prods>> getPartBoxByDatePageble(@Query("date") String date, @Query("userId") int userId, @Query("deviceId") String deviceId, @Query("page") int page);
    @GET("/pbByDatePagebleCount")
    Call<Integer> getPartBoxByDatePagebleCount(@Query("date") String date);

    @GET("/division")
    Call<List<Division>> getDivision();

    @GET("/employee/v2")
    Call<List<Sotr>> getSotr(@Query("date") String date);

    @GET("/user/v2")
    Call<List<user>> getUser(@Query("date") String date);

    @GET("/department")
    Call<List<Deps>> getDeps(@Query("date") String date);

    @GET("/operation")
    Call<List<Operation>> getOperation(@Query("date") String date);

    @POST("/outDocSaveOrUpdate/v3")
    Call<List<OutDocs>> addOutDoc(@Body ArrayList<OutDocs> outDocs, @Query("deviceId") String deviceId);

    @GET("/outDocGet")
    Call<List<OutDocs>> getOutDocGet(@Query("date") String date);

    @GET("/serverUpdateTime")
    Call<Long> getServerUpdateTime();

    @GET("/")
    Call<Object> checkConnection();
}