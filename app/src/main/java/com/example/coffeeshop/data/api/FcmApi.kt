
package com.example.coffeeshop.data.api

import com.example.coffeeshop.data.models.FcmTokenRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("/fcm/savetoken")
    fun saveToken(@Body fcmTokenRequest: FcmTokenRequest): Call<Void>
}
