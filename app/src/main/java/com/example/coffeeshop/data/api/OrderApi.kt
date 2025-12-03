package com.example.coffeeshop.data.api

import com.example.coffeeshop.data.model.OrderDTO
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body order: OrderDTO): Response<OrderDTO>

    @GET("orders")
    suspend fun getOrders(
        @Query("userId") userId: String,
        @Query("status") status: String
    ): Response<List<OrderDTO>>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Query("status") newStatus: String
    ): Response<Unit>
}
