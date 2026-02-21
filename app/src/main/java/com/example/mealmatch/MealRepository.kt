package com.example.mealmatch

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MealRepository {

    suspend fun getRandomMeals(): List<Meal> {
        return RetrofitClient.api.getRandomMeals().meals ?: emptyList()
    }

    suspend fun searchAll(query: String): List<Meal> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        if (q.length == 1 && q[0].isLetter()) {
            return RetrofitClient.api
                .searchByFirstLetter(q.lowercase())
                .meals ?: emptyList()
        }

        return coroutineScope {
            val byNameDeferred = async {
                RetrofitClient.api.searchByMealName(q).meals ?: emptyList()
            }

            val byIngDeferred = async {
                val ingredient = q.lowercase().replace(" ", "_")
                (RetrofitClient.api.filterByIngredient(ingredient).meals ?: emptyList())
                    .map { it.toMeal() }
            }

            val merged = byNameDeferred.await() + byIngDeferred.await()
            merged.distinctBy { it.idMeal }
        }
    }

    // ✅ NEW: multi filter (categories + areas)
    suspend fun filterMulti(categories: List<String>, areas: List<String>): List<Meal> = coroutineScope {
        val cat = categories.map { it.trim() }.filter { it.isNotEmpty() }
        val ar = areas.map { it.trim() }.filter { it.isNotEmpty() }

        if (cat.isEmpty() && ar.isEmpty()) return@coroutineScope emptyList()

        val categoriesDeferred = async {
            if (cat.isEmpty()) emptyList()
            else {
                val all = cat.flatMap { c ->
                    (RetrofitClient.api.filterByCategory(c).meals ?: emptyList())
                }
                all.distinctBy { it.idMeal }.map { it.toMeal() }
            }
        }

        val areasDeferred = async {
            if (ar.isEmpty()) emptyList()
            else {
                val all = ar.flatMap { a ->
                    (RetrofitClient.api.filterByArea(a).meals ?: emptyList())
                }
                all.distinctBy { it.idMeal }.map { it.toMeal() }
            }
        }

        val catMeals = categoriesDeferred.await()
        val areaMeals = areasDeferred.await()

        val result = when {
            cat.isNotEmpty() && ar.isNotEmpty() -> {
                val areaSet = areaMeals.map { it.idMeal }.toHashSet()
                catMeals.filter { areaSet.contains(it.idMeal) }
            }
            cat.isNotEmpty() -> catMeals
            else -> areaMeals
        }

        result.distinctBy { it.idMeal }
    }
}