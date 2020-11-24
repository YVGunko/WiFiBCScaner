package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 12.12.2017.
 */

import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface RetrofitIterface {
    @GET
    Call<ResponseBody> getData(@Url String url, @Query("barcode") String barcode);
}
