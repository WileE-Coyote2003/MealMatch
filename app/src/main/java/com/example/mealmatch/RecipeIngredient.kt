package com.example.mealmatch

data class RecipeIngredient(
    val name: String,
    val amount: String,
    val imageRes: Int? = null
)