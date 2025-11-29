package com.example.coffeeshop.data.api

import com.example.coffeeshop.data.model.Item
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("item/coffee/getall")
    fun getCoffeeItems(): Call<List<Item>>

    @GET("item/chocolate/getall")
    fun getChocolateItems(): Call<List<Item>>

    @GET("item/other/getall")
    fun getOtherItems(): Call<List<Item>>
}