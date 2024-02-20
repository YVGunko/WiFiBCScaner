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
import retrofit2.http.Query;

import com.example.yg.wifibcscaner.data.BoxMoves;
import com.example.yg.wifibcscaner.data.Boxes;
import com.example.yg.wifibcscaner.data.Deps;
import com.example.yg.wifibcscaner.data.Division;
import com.example.yg.wifibcscaner.data.Operation;
import com.example.yg.wifibcscaner.data.Orders;
import com.example.yg.wifibcscaner.data.OutDocs;
import com.example.yg.wifibcscaner.data.Prods;
import com.example.yg.wifibcscaner.data.Sotr;
import com.example.yg.wifibcscaner.data.user;

public interface OrderService {

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