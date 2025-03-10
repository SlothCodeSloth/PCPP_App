package com.example.pcpartpicker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PyPartPickerApi {
    @GET("/search")
    fun searchParts(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("region") region: String
    ): Call<List<Component.Part>>

    @GET("/product")
    fun fetchProduct(@Query("url") url: String): Call<Component.Product>
}