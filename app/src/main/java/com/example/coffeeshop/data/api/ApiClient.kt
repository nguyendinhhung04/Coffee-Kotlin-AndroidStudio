package com.example.coffeeshop.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://c76lgf-3000.csb.app/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService cũ
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // OrderApi mới – CHÚ Ý tên biến là orderApi
    val orderApi: OrderApi by lazy {
        retrofit.create(OrderApi::class.java)
    }
}
