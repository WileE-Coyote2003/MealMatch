package com.example.mealmatch

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RecipeDetails : AppCompatActivity() {

    companion object {
        const val EXTRA_MEAL_ID = "extra_meal_id"
    }

    private var isSaved = false
    private var isCooked = false

    private val repo = MealRepository()

    private var youtubeUrl: String = ""

    // Views we will reuse
    private lateinit var btnSaveTop: ImageButton
    private lateinit var btnCookedTop: ImageButton
    private lateinit var btnSaveRecipe: MaterialButton
    private lateinit var btnCookedRecipe: MaterialButton
    private lateinit var btnShowVideo: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mealId = intent.getStringExtra(EXTRA_MEAL_ID)
        if (mealId.isNullOrBlank()) {
            finish()
            return
        }

        setupButtons()
        loadMealDetail(mealId)
    }

    private fun setupButtons() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val saveContainer = findViewById<LinearLayout>(R.id.save_btn)
        val btnClose = findViewById<MaterialButton>(R.id.btnCloseRecipe)

        btnSaveTop = findViewById(R.id.btnSave)
        btnCookedTop = findViewById(R.id.btnCooked)

        btnSaveRecipe = findViewById(R.id.btnSaveRecipe)
        btnCookedRecipe = findViewById(R.id.btnCookedRecipe)

        btnShowVideo = findViewById(R.id.btnShowVideo)

        btnBack.bringToFront()
        saveContainer.bringToFront()

        btnBack.setOnClickListener { finish() }
        btnClose.setOnClickListener { finish() }

        // initial UI
        updateSaveUI(btnSaveRecipe, btnSaveTop)
        updateCookedUI(btnCookedRecipe, btnCookedTop)

        // SAVE toggles
        btnSaveTop.setOnClickListener {
            isSaved = !isSaved
            updateSaveUI(btnSaveRecipe, btnSaveTop)
        }
        btnSaveRecipe.setOnClickListener {
            isSaved = !isSaved
            updateSaveUI(btnSaveRecipe, btnSaveTop)
        }

        // COOKED toggles
        btnCookedTop.setOnClickListener {
            isCooked = !isCooked
            updateCookedUI(btnCookedRecipe, btnCookedTop)
        }
        btnCookedRecipe.setOnClickListener {
            isCooked = !isCooked
            updateCookedUI(btnCookedRecipe, btnCookedTop)
        }

        // VIDEO (will be enabled/disabled after API loads)
        btnShowVideo.isEnabled = false
        btnShowVideo.alpha = 0.4f
        btnShowVideo.setOnClickListener {
            if (youtubeUrl.isBlank()) return@setOnClickListener
            openUrl(youtubeUrl)
        }
    }

    private fun loadMealDetail(mealId: String) {
        lifecycleScope.launch {
            val meal = repo.getMealDetail(mealId) ?: return@launch

            findViewById<TextView>(R.id.food_title).text = meal.strMeal

            Glide.with(this@RecipeDetails)
                .load(meal.strMealThumb)
                .into(findViewById(R.id.ivHeader))

            // ========================
            // YOUTUBE
            // ========================
            youtubeUrl = meal.strYoutube?.trim().orEmpty()
            btnShowVideo.isEnabled = youtubeUrl.isNotBlank()
            btnShowVideo.alpha = if (youtubeUrl.isNotBlank()) 1f else 0.4f

            // ========================
            // INGREDIENTS
            // ========================
            val ingredients = mutableListOf<RecipeIngredient>()
            for (i in 1..20) {
                val ingredient = meal.getIngredient(i)?.trim()
                val measure = meal.getMeasure(i)?.trim().orEmpty()

                if (!ingredient.isNullOrBlank()) {
                    ingredients.add(
                        RecipeIngredient(
                            name = ingredient,
                            amount = measure,
                            imageRes = null
                        )
                    )
                }
            }

            val rvIngredients = findViewById<RecyclerView>(R.id.rvIngredients)
            rvIngredients.layoutManager = GridLayoutManager(this@RecipeDetails, 2)
            rvIngredients.isNestedScrollingEnabled = false
            rvIngredients.adapter = RecipeIngredientAdapter(ingredients)

            // ========================
            // STEPS (remove "Step 1" lines & prefix)
            // ========================
            val rawLines = meal.strInstructions
                .orEmpty()
                .split("\r\n", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val withoutStepOnlyLines = rawLines.filterNot { line ->
                line.matches(Regex("^step\\s*\\d+\\s*[:.]*$", RegexOption.IGNORE_CASE))
            }

            val cleanedLines = withoutStepOnlyLines
                .map { line ->
                    line.replace(
                        Regex("^step\\s*\\d+\\s*[:.\\-]*\\s*", RegexOption.IGNORE_CASE),
                        ""
                    ).trim()
                }
                .filter { it.isNotBlank() }

            val steps = cleanedLines.mapIndexed { index, text ->
                CookingSteps(index + 1, "", text)
            }

            val rvSteps = findViewById<RecyclerView>(R.id.rvCookingSteps)
            rvSteps.layoutManager = LinearLayoutManager(this@RecipeDetails)
            rvSteps.isNestedScrollingEnabled = false
            rvSteps.adapter = CookingStepAdapter(steps)
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    // ===============================
    // SAVE UI
    // ===============================
    private fun updateSaveUI(btnSaveRecipe: MaterialButton, btnSaveTop: ImageButton) {
        val orange = ContextCompat.getColor(this, R.color.orange)

        if (isSaved) {
            btnSaveRecipe.text = "Saved"
            btnSaveRecipe.setTextColor(Color.WHITE)
            btnSaveRecipe.backgroundTintList = ColorStateList.valueOf(orange)
            btnSaveRecipe.iconTint = ColorStateList.valueOf(Color.WHITE)
            btnSaveRecipe.strokeWidth = 0

            btnSaveTop.backgroundTintList = ColorStateList.valueOf(orange)
            btnSaveTop.imageTintList = ColorStateList.valueOf(Color.WHITE)

        } else {
            btnSaveRecipe.text = "Save Recipe"
            btnSaveRecipe.setTextColor(Color.BLACK)
            btnSaveRecipe.backgroundTintList = ColorStateList.valueOf(Color.WHITE)

            val strokeWidthPx = (1 * resources.displayMetrics.density).toInt()
            btnSaveRecipe.strokeWidth = strokeWidthPx
            btnSaveRecipe.strokeColor = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btnSaveRecipe.iconTint = ColorStateList.valueOf(Color.BLACK)

            btnSaveTop.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            btnSaveTop.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }
    }

    // ===============================
    // COOKED UI (your requested style)
    // Before: black bg + white text
    // After : green text + green bg 20% + green stroke 20%
    // Top icon after: green bg + white icon
    // ===============================
    private fun updateCookedUI(btnCookedRecipe: MaterialButton, btnCookedTop: ImageButton) {
        val green = Color.parseColor("#22C55E")
        val green20 = Color.argb(
            51, // 20% opacity
            Color.red(green),
            Color.green(green),
            Color.blue(green)
        )

        val strokeWidthPx = (1 * resources.displayMetrics.density).toInt()

        if (isCooked) {
            btnCookedRecipe.text = "Cooked"
            btnCookedRecipe.setTextColor(green)
            btnCookedRecipe.backgroundTintList = ColorStateList.valueOf(green20)
            btnCookedRecipe.strokeWidth = strokeWidthPx
            btnCookedRecipe.strokeColor = ColorStateList.valueOf(green20)

            btnCookedTop.backgroundTintList = ColorStateList.valueOf(green)
            btnCookedTop.imageTintList = ColorStateList.valueOf(Color.WHITE)

        } else {
            btnCookedRecipe.text = "Mark as Cooked"
            btnCookedRecipe.setTextColor(Color.WHITE)
            btnCookedRecipe.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
            btnCookedRecipe.strokeWidth = 0

            btnCookedTop.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            btnCookedTop.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }
    }
}