package com.example.mealmatch

data class MealFilterResponse(
    val meals: List<MealFilterItem>?
)

data class MealFilterItem(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)

// helper to convert filter items -> Meal so your adapter uses one type
fun MealFilterItem.toMeal(): Meal = Meal(
    idMeal = idMeal,
    strMeal = strMeal,
    strMealThumb = strMealThumb
)