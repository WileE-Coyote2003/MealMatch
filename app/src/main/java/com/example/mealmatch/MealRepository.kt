package com.example.mealmatch

class MealRepository {

    suspend fun getRandomMeals(): List<Meal> {
        return RetrofitClient.api.getRandomMeals().meals
    }
}
