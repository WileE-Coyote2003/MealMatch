package com.example.mealmatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealViewModel : ViewModel() {

    private val repository = MealRepository()

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    fun loadRandomMeals() {
        viewModelScope.launch {
            try {
                _meals.value = repository.getRandomMeals()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
