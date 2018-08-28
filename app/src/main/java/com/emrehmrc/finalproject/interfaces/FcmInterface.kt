package com.emrehmrc.finalproject.interfaces

import com.emrehmrc.finalproject.class_model.FcmNotification
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface FcmInterface {

    @POST("send")
        fun sendNotification(

            @HeaderMap headers:Map<String,String>,
            @Body notificationMessage: FcmNotification

    ):Call<Response<FcmNotification>>


}