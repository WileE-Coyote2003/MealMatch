package com.example.mealmatch

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MealRepository {

    suspend fun getRandomMeals(): List<Meal> {
        return RetrofitClient.api.getRandomMeals().meals ?: emptyList()
    }

    /**
     * One search box for:
     * - first letter (if 1 char)
     * - meal name + ingredient (merge results, remove duplicates)
     */
    suspend fun searchAll(query: String): List<Meal> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        // 1 letter => first-letter search
        if (q.length == 1 && q[0].isLetter()) {
            return RetrofitClient.api
                .searchByFirstLetter(q.lowercase())
                .meals ?: emptyList()
        }

        // otherwise: do both name + ingredient and merge
        return coroutineScope {
            val byNameDeferred = async {
                RetrofitClient.api.searchByMealName(q).meals ?: emptyList()
            }

            val byIngDeferred = async {
                // TheMealDB ingredient often uses underscore
                val ingredient = q.lowercase().replace(" ", "_")
                (RetrofitClient.api.filterByIngredient(ingredient).meals ?: emptyList())
                    .map { it.toMeal() }
            }

            val merged = byNameDeferred.await() + byIngDeferred.await()
            merged.distinctBy { it.idMeal }
        }
    }
}