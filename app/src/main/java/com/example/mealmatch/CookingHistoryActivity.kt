package com.example.mealmatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CookingHistoryActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var btnBack: ImageButton
    private lateinit var rv: RecyclerView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var tvPage: TextView
    private lateinit var tvTitle: TextView

    private lateinit var adapter: SearchMealAdapter

    private var allMeals: List<Meal> = emptyList()
    private var currentPage = 1
    private val pageSize = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites) // reuse same layout

        btnBack = findViewById(R.id.btnBack)
        rv = findViewById(R.id.rvAllResults)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvPage = findViewById(R.id.tvPage)
        tvTitle = findViewById(R.id.tvResultsTitle)

        tvTitle.text = "Cooking History"

        btnBack.setOnClickListener { finish() }

        adapter = SearchMealAdapter(mutableListOf()) { meal ->
            val intent = Intent(this, RecipeDetails::class.java)
            intent.putExtra(RecipeDetails.EXTRA_MEAL_ID, meal.idMeal)
            startActivity(intent)
        }

        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                renderPage()
                rv.scrollToPosition(0)
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages()) {
                currentPage++
                renderPage()
                rv.scrollToPosition(0)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser ?: return

        db.collection("users")
            .document(user.uid)
            .collection("cooked")   // 🔥 important: cooked collection
            .orderBy("cookedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->

                val list = mutableListOf<Meal>()

                if (snap != null) {
                    for (doc in snap.documents) {
                        val id = doc.getString("mealId").orEmpty()
                        val name = doc.getString("name").orEmpty()
                        val thumb = doc.getString("thumb").orEmpty()

                        if (id.isNotBlank()) {
                            list.add(
                                Meal(
                                    idMeal = id,
                                    strMeal = name,
                                    strMealThumb = thumb
                                )
                            )
                        }
                    }
                }

                allMeals = list
                currentPage = 1

                tvTitle.text = "Cooking History (${allMeals.size})"

                renderPage()
            }
    }

    private fun totalPages(): Int {
        return (allMeals.size + pageSize - 1) / pageSize
    }

    private fun renderPage() {
        if (allMeals.isEmpty()) {
            adapter.setMeals(emptyList())
            tvPage.text = "Page 0 / 0"
            btnPrev.isEnabled = false
            btnNext.isEnabled = false
            return
        }

        val from = (currentPage - 1) * pageSize
        val to = minOf(currentPage * pageSize, allMeals.size)
        val pageMeals = allMeals.subList(from, to)

        adapter.setMeals(pageMeals)

        val total = totalPages().coerceAtLeast(1)
        tvPage.text = "Page $currentPage / $total"

        btnPrev.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < total
    }
}