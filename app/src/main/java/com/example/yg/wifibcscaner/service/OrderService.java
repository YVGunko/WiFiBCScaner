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

import com.example.yg.wifibcscaner.BoxMoves;
import com.example.yg.wifibcscaner.Boxes;
import com.example.yg.wifibcscaner.Deps;
import com.example.yg.wifibcscaner.Division;
import com.example.yg.wifibcscaner.data.Operation;
import com.example.yg.wifibcscaner.Orders;
import com.example.yg.wifibcscaner.OutDocs;
import com.example.yg.wifibcscaner.Prods;
import com.example.yg.wifibcscaner.Sotr;
import com.example.yg.wifibcscaner.user;

public interface OrderService {

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

    @GET("/orderUser")
    Call<List<Orders>> getOrdersUser(@Query("date") String date, @Query("userId") int userId);
    @GET("/boxesByDateUser")
    Call<List<Boxes>> getBoxesByDateUser(@Query("date") String date, @Query("userId") int userId);
    @GET("/bmByDateUser")
    Call<List<BoxMoves>> getBoxMovesByDateUser(@Query("date") String date, @Query("userId") int userId);
    @GET("/pbByDateUser")
    Call<List<Prods>> getPartBoxByDateUser(@Query("date") String date, @Query("userId") int userId);

    @POST("/orders")
    Call<List<Orders>> getAllOrders(@Body ArrayList<String> ordersId);

    @POST("/archiveOrders")
    Call<List<String>> getArchiveOrders(@Body ArrayList<String> ordersId);

    @GET("/division")
    Call<List<Division>> getDivision();

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