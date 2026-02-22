package com.example.mealmatch

import retrofit2.http.GET
import retrofit2.http.Query
interface MealApiService {
    // Random selection
    @GET("api/json/v2/65232507/randomselection.php")
    suspend fun getRandomMeals(): MealResponse

    @GET("api/json/v2/65232507/random.php")
    suspend fun getSingleRandomMeals(): MealResponse

    // Search by meal name
    @GET("api/json/v2/65232507/search.php")
    suspend fun searchByMealName(@Query("s") name: String): MealResponse

    // Search by first letter
    @GET("api/json/v2/65232507/search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): MealResponse

    // Search by ingredient
    @GET("api/json/v2/65232507/filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealFilterResponse

    // Filter by category
    @GET("api/json/v2/65232507/filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealFilterResponse

    // Filter by area
    @GET("api/json/v2/65232507/filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealFilterResponse

    @GET("api/json/v2/65232507/lookup.php")
    suspend fun getMealDetail(@Query("i") id: String): MealDetailResponse
}
