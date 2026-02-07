package com.example.mealmatch

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.content.Intent
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val viewModel: MealViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter
    private lateinit var ingredientAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // =========================
        // Sign In Button
        // =========================
        val signInBtn = findViewById<MaterialButton>(R.id.signInBtn)
        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // =========================
        // Recommended Meals
        // =========================
        val mealRecyclerView = findViewById<RecyclerView>(R.id.rvRecommendedMeals)

        mealAdapter = MealAdapter(mutableListOf()) { meal ->
            // TODO: Navigate to detail page using meal.idMeal
        }

        mealRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mealRecyclerView.adapter = mealAdapter

        lifecycleScope.launch {
            viewModel.meals.collect { meals ->
                mealAdapter.updateMeals(meals)
            }
        }

        viewModel.loadRandomMeals()

        // =========================
        // Fresh Ingredients (Static)
        // =========================
        val ingredientRecyclerView = findViewById<RecyclerView>(R.id.rvIngredients)

        val ingredients = listOf(
            Ingredient("Chicken", "https://www.themealdb.com/images/ingredients/chicken.png"),
            Ingredient("Salmon", "https://www.themealdb.com/images/ingredients/salmon.png"),
            Ingredient("Beef", "https://www.themealdb.com/images/ingredients/beef.png"),
            Ingredient("Pork", "https://www.themealdb.com/images/ingredients/pork.png"),
            Ingredient("Eggs", "https://www.themealdb.com/images/ingredients/eggs.png"),
            Ingredient("Rice", "https://www.themealdb.com/images/ingredients/sushi_rice.png"),
            Ingredient("Turkey Ham", "https://www.themealdb.com/images/ingredients/Turkey_Ham.png"),
            Ingredient("Corn Flour", "https://www.themealdb.com/images/ingredients/corn_flour.png")
        )

        ingredientAdapter = IngredientAdapter(ingredients)

        ingredientRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ingredientRecyclerView.adapter = ingredientAdapter
    }
}
