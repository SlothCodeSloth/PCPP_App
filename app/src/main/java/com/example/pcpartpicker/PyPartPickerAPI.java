package com.example.pcpartpicker;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PyPartPickerAPI {
    @GET("/search")
    Call<List<Component.Part>> searchParts(@Query("query") String query, @Query("limit") int limit, @Query("region") String region);

    @GET("/product")
    Call<Component.Product> fetchProduct(@Query("url") String url);
}
