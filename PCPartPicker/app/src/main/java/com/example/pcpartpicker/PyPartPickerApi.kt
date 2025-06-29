package com.example.pcpartpicker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PyPartPickerApi {
    @GET("/search")
    /*
    fun searchParts(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("region") region: String
    ): Call<List<Component.Part>>

    @GET("/product")
    fun fetchProduct(@Query("url") url: String): Call<Component.Product>
     */

    suspend fun searchParts(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int = 1,
        @Query("region") region: String = "us"
    ): SearchResponse

    @GET("/product")
    suspend fun fetchProduct(@Query("url") url: String): ProductResponse
}

data class SearchResponse(
    val results: List<Component.Part>,
    val page: Int,
    val total_pages: Int
)

data class ProductResponse(
    val name: String,
    val specs: Map<String, String>,
    val price_list: List<Vendor>,
    val image: String?,
    val rating: Rating?
)

data class Vendor(
    val seller: String,
    val value: Double,
    val in_stock: Boolean
)

data class Rating(
    val average: Double?,
    val count: Int?
)