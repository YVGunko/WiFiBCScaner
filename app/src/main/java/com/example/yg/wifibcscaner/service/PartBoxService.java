package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 15.12.2017.
 */

import com.example.yg.wifibcscaner.data.dto.PartBoxRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface PartBoxService {

    @POST("/partBox")
    Call<PartBoxRequest> addBoxes(@Body PartBoxRequest partBoxRequest, @Query("userId") int userId, @Query("deviceId") String deviceId);

    @GET("/")
    Call<Object> checkConnection();
}