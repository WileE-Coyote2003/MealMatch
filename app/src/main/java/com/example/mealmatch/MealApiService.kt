package com.example.mealmatch

import retrofit2.http.GET

interface MealApiService {

    @GET("api/json/v2/65232507/randomselection.php")
    suspend fun getRandomMeals(): MealResponse

}
