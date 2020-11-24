package com.example.yg.wifibcscaner.service;

/**
 * Created by yg on 12.12.2017.
 */

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class RetrofitClass {
    private static String baseUrl;

    public void setUrl(String baseUrl){
        this.baseUrl =baseUrl;
    }

    public static RetrofitIterface getApi() throws Exception {
        if (baseUrl.isEmpty() || baseUrl==null){
            throw new Exception("baseUrl for api call in retrofit class is not set");
        }
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();

        RetrofitIterface RetInt = retrofit.create(RetrofitIterface.class);
        return RetInt;
    }
}