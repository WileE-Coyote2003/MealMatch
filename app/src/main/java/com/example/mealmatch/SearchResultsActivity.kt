package com.example.mealmatch

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import android.content.Intent
class SearchResultsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "extra_query"
        const val EXTRA_MEALS = "extra_meals"
    }

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var rvAllResults: RecyclerView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var tvPage: TextView

    private lateinit var adapter: SearchMealAdapter

    private var allMeals: List<Meal> = emptyList()
    private var query: String = ""
    private var currentPage = 1
    private val pageSize = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        query = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        allMeals = intent.getParcelableArrayListExtra<Meal>(EXTRA_MEALS) ?: emptyList()

        tvTitle = findViewById(R.id.tvResultsTitle)
        btnBack = findViewById(R.id.btnBack)
        rvAllResults = findViewById(R.id.rvAllResults)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvPage = findViewById(R.id.tvPage)

        tvTitle.text = "Results for \"$query\" (${allMeals.size})"

        btnBack.setOnClickListener { finish() }

        adapter = SearchMealAdapter(mutableListOf()) { meal ->
            val intent = Intent(this, RecipeDetails::class.java)
            intent.putExtra(RecipeDetails.EXTRA_MEAL_ID, meal.idMeal)
            startActivity(intent)
        }

        rvAllResults.layoutManager = GridLayoutManager(this, 2)
        rvAllResults.adapter = adapter

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                renderPage()
                rvAllResults.scrollToPosition(0)
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages()) {
                currentPage++
                renderPage()
                rvAllResults.scrollToPosition(0)
            }
        }

        renderPage()
    }

    private fun totalPages(): Int {
        return (allMeals.size + pageSize - 1) / pageSize
    }

    private fun renderPage() {
        val from = (currentPage - 1) * pageSize
        val to = minOf(currentPage * pageSize, allMeals.size)
        val pageMeals = if (from in 0..allMeals.size) allMeals.subList(from, to) else emptyList()

        adapter.setMeals(pageMeals)

        val total = totalPages().coerceAtLeast(1)
        tvPage.text = "Page $currentPage / $total"

        btnPrev.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < total
    }
}