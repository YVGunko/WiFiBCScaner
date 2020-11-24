package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 15.12.2017.
 */
import com.example.yg.wifibcscaner.Boxes;
import com.example.yg.wifibcscaner.Orders;
import com.example.yg.wifibcscaner.OutDocs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface PartBoxService {

    @POST("/partBox")
    Call<PartBoxRequest> addBoxes(@Body PartBoxRequest partBoxRequest);

    /*@POST("/boxSync")
    Call<PartBoxRequest> boxSync(@Query("date") String date);

    @GET("/partBoxCheck")
    Call<PartBoxRequest> partBoxCheck(@Body PartBoxRequest partBoxRequest);*/

    @GET("/")
    Call<Object> checkConnection();

    /*@POST("/boxSyncPageable")
    Call<PartBoxRequest> boxSyncPageable(@Query("date") String date, @Query ("pageNumber") int pageNumber);

    @GET("/boxSyncPageableCount")
    Call<Integer> boxSyncPageableCount(@Query("date") String date);*/
}