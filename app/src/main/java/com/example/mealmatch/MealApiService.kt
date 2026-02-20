package com.example.mealmatch

import retrofit2.http.GET
import retrofit2.http.Query
interface MealApiService {
    // Random selection
    @GET("api/json/v2/65232507/randomselection.php")
    suspend fun getRandomMeals(): MealResponse

    // Search by meal name
    @GET("api/json/v2/65232507/search.php")
    suspend fun searchByMealName(@Query("s") name: String): MealResponse

    // Search by first letter
    @GET("api/json/v2/65232507/search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): MealResponse

    // Search by ingredient
    @GET("api/json/v2/65232507/filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealFilterResponse


}
