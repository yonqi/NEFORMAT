package com.example.neformat;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/events")
    Call<List<Event>> getEvents();  // Предполагается, что у тебя есть модель Event

    @GET("designs/by-url")
    Call<Design> getDesignByUrl(@Query("url") String url);

    @POST("api/favorites/add")  // Путь должен совпадать с серверным!
    Call<Void> addToFavorites(@Body FavoriteRequest request);

    @HTTP(method = "DELETE", path = "api/favorites/remove", hasBody = true)
    Call<Void> removeFromFavorites(@Body FavoriteRequest request);
}

