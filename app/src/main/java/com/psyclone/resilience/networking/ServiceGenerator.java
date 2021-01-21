package com.psyclone.resilience.networking;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static final String BASE_URL = "https://resilience-server.herokuapp.com/";

    // Todo: Look into this timeout stuff
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).build();

    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient);

    private static Retrofit retrofit = retrofitBuilder.build();

    private static GroupMessageApi groupMessageApi = retrofit.create(GroupMessageApi.class);

    public static GroupMessageApi getGroupMessageApi(){
        return groupMessageApi;
    }
}
