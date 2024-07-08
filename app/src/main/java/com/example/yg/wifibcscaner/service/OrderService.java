package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 23.01.2018.
 */


import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Sotr;
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
    Call<PartBoxRequest> partBox(@Body PartBoxRequest partBoxRequest, @Query("userId") int userId, @Query("deviceId") String deviceId);

    @GET("/division")
    Call<List<Division>> getDivision();

    @GET("/employee/v2")
    Call<List<Sotr>> getSotr(@Query("date") String date);

    @GET("/user/v2")
    Call<List<user>> getUser();

    @GET("/department")
    Call<List<Deps>> getDeps(@Query("date") String date);

    @GET("/operation")
    Call<List<Operation>> getOperation();

    @POST("/outDocSaveOrUpdate/v3")
    Call<List<OutDocs>> addOutDoc(@Body ArrayList<OutDocs> outDocs, @Query("deviceId") String deviceId);

    @GET("/serverUpdateTime")
    Call<Long> getServerUpdateTime();

    @GET("/")
    Call<Object> checkConnection();
}