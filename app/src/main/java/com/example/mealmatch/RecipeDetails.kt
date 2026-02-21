package com.example.mealmatch

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RecipeDetails : AppCompatActivity() {

    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_details)

        val root = findViewById<android.view.View?>(R.id.main)
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val saveContainer = findViewById<LinearLayout>(R.id.save_btn)
        val btnSaveTop = findViewById<ImageButton>(R.id.btnSave)
        val btnSaveRecipe = findViewById<MaterialButton>(R.id.btnSaveRecipe)

        btnBack.bringToFront()
        saveContainer.bringToFront()

        btnBack.setOnClickListener { finish() }

        // Initial state
        updateSaveUI(btnSaveRecipe, btnSaveTop)

        // Toggle from top heart
        btnSaveTop.setOnClickListener {
            isSaved = !isSaved
            updateSaveUI(btnSaveRecipe, btnSaveTop)
        }

        // Toggle from bottom button
        btnSaveRecipe.setOnClickListener {
            isSaved = !isSaved
            updateSaveUI(btnSaveRecipe, btnSaveTop)
        }

        // Ingredients
        val rv = findViewById<RecyclerView>(R.id.rvIngredients)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.isNestedScrollingEnabled = false

        val items = listOf(
            RecipeIngredient("Beef Shin", "1KG", R.drawable.food3),
            RecipeIngredient("Onion", "1", R.drawable.food2),
            RecipeIngredient("Potato", "2", R.drawable.food12),
            RecipeIngredient("Bay Leaf", "3", R.drawable.food3),
        )
        rv.adapter = RecipeIngredientAdapter(items)

        // Steps
        val rvSteps = findViewById<RecyclerView>(R.id.rvCookingSteps)
        rvSteps.layoutManager = LinearLayoutManager(this)
        rvSteps.isNestedScrollingEnabled = false

        val steps = listOf(
            CookingSteps(1, "step 1", "To make the stock, put the meat, whole onion, bay leaf..."),
            CookingSteps(2, "step 2", "Cook over a very low heat for 1 hr 30 mins..."),
            CookingSteps(3, "step 3", "Skim off the scum with a spoon...")
        )
        rvSteps.adapter = CookingStepAdapter(steps)

        val btnClose = findViewById<MaterialButton>(R.id.btnCloseRecipe)
        btnClose.setOnClickListener { finish() }
    }

    private fun updateSaveUI(btnSaveRecipe: MaterialButton, btnSaveTop: ImageButton) {

        val orange = ContextCompat.getColor(this, R.color.orange)
        val strokeLight = Color.parseColor("#E0E0E0") // light border

        if (isSaved) {

            // ===== Bottom button (Saved) =====
            btnSaveRecipe.text = "Saved"
            btnSaveRecipe.setTextColor(Color.WHITE)
            btnSaveRecipe.backgroundTintList = ColorStateList.valueOf(orange)
            btnSaveRecipe.iconTint = ColorStateList.valueOf(Color.WHITE)

            // remove stroke when saved (optional)
            btnSaveRecipe.strokeWidth = 0

            // ===== Top heart =====
            btnSaveTop.backgroundTintList = ColorStateList.valueOf(orange)
            btnSaveTop.imageTintList = ColorStateList.valueOf(Color.WHITE)

        } else {

            btnSaveRecipe.text = "Save Recipe"
            btnSaveRecipe.setTextColor(Color.BLACK)

            // White background
            btnSaveRecipe.backgroundTintList = ColorStateList.valueOf(Color.WHITE)

            // Light stroke
            val strokeLight = Color.parseColor("#E0E0E0")
            val strokeWidthPx = (1 * resources.displayMetrics.density).toInt()

            btnSaveRecipe.strokeWidth = strokeWidthPx
            btnSaveRecipe.strokeColor = ColorStateList.valueOf(strokeLight)

            btnSaveRecipe.iconTint = ColorStateList.valueOf(Color.BLACK)

            btnSaveTop.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            btnSaveTop.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }
    }
}
