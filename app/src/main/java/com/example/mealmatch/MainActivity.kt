package com.example.mealmatch

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: MealViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter
    private lateinit var ingredientAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        // browse by name
//        val letters = listOf(
//            findViewById<TextView>(R.id.filter_all),
//        )
//        letters.forEach { tv ->
//            tv.setOnClickListener {
//                val letter = tv.text.toString()
//                // filter your list here
//                Toast.makeText(this, "Filter: $letter", Toast.LENGTH_SHORT).show()
//            }
//        }
        //footer year
        val year = Calendar.getInstance().get(Calendar.YEAR)
        findViewById<TextView>(R.id.footer_year).text="© $year MealMatch. All rights reserved."
        ingredientAdapter = IngredientAdapter(ingredients)

        ingredientRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ingredientRecyclerView.adapter = ingredientAdapter
    }
}
