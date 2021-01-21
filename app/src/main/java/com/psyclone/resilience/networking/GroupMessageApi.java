package com.psyclone.resilience.networking;

import com.psyclone.resilience.models.Message;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GroupMessageApi {
    @FormUrlEncoded
    @POST("message/send")
    Call<Message> sendMessage(@Field("message") String message, @Field("topic") String topic,
                              @Field("user") String user, @Field("timestamp") long timestamp);

    @GET("message/subscribe/{topic}/{token}")
    Call<String> subscribeToTopic(@Path("topic") String topic, @Path("token") String token);

    @GET("message/all/{topic}/{token}")
    Call<ArrayList<Message>> getAllTopicMessage(@Path("topic") String topic, @Path("token") String token);
}

