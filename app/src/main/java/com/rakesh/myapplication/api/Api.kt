package com.rakesh.myapplication.api

import com.rakesh.myapplication.model.DefaultResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {

    @FormUrlEncoded
    @POST("sendQR")
    fun sendData(
        @Field("QRData") email: String
    ): Call<DefaultResponse>
}