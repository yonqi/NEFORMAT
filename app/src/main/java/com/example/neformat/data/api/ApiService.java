package com.example.neformat.data.api;


import com.example.neformat.data.model.Design;
import com.example.neformat.data.model.FavoriteRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/favorites/check")
    Call<Boolean> checkFavorite(
            @Query("firebaseUid") String firebaseUid,
            @Query("imageUrl") String imageUrl
    );

    @GET("designs/by-url")
    Call<Design> getDesignByUrl(@Query("url") String url);

    @POST("api/favorites/add")
    Call<Void> addToFavorites(@Body FavoriteRequest request);

    @HTTP(method = "DELETE", path = "api/favorites/remove", hasBody = true)
    Call<Void> removeFromFavorites(@Body FavoriteRequest request);
}

