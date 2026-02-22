package com.example.mealmatch

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity(), MealFilterBottomSheet.FilterListener {

    private val viewModel: MealViewModel by viewModels()

    // Trending (horizontal)
    private lateinit var rvTrendingMeals: RecyclerView
    private lateinit var mealAdapter: MealAdapter

    // Search results (grid preview on home)
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var searchAdapter: SearchMealAdapter

    // Ingredients (horizontal)
    private lateinit var rvIngredients: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var signInBtn: MaterialButton
    private lateinit var profileBtn: ShapeableImageView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rightDrawer: NavigationView

    // Navbar scroll behavior
    private lateinit var scrollView: NestedScrollView
    private lateinit var topNavbar: ConstraintLayout
    private lateinit var tvMeal: TextView
    private lateinit var tvMatch: TextView
    private var isScrolledStyle = false

    // Search + Title
    private lateinit var searchEditText: TextInputEditText
    private lateinit var trendingTitle: TextView
    private lateinit var viewAllText: TextView

    private val repo = MealRepository()
    private var job: Job? = null

    // Keep last trending list for restore
    private var latestTrendingMeals: List<Meal> = emptyList()

    // For View All screen (Search / A-Z / Ingredient / Filter all use this)
    private var lastQueryTitle: String = ""
    private var lastResults: ArrayList<Meal> = arrayListOf()

    private val HOME_PREVIEW_LIMIT = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        rightDrawer = findViewById(R.id.rightDrawer)

        // Navbar
        topNavbar = findViewById(R.id.topnavbar)
        tvMeal = findViewById(R.id.tvMeal)
        tvMatch = findViewById(R.id.tvMatch)
        signInBtn = findViewById(R.id.signInBtn)
        profileBtn = findViewById(R.id.profileBtn)

        // Scroll
        scrollView = findViewById(R.id.mainContent)
        setupNavbarScrollBehavior()

        // Title + Search
        trendingTitle = findViewById(R.id.trendingTitle)
        viewAllText = findViewById(R.id.viewAllText)
        searchEditText = findViewById(R.id.searchEditText)

        // Trending RV
        rvTrendingMeals = findViewById(R.id.rvRecommendedMeals)
        mealAdapter = MealAdapter(mutableListOf()) { meal ->
            val intent = Intent(this, RecipeDetails::class.java)
            intent.putExtra(RecipeDetails.EXTRA_MEAL_ID, meal.idMeal)
            startActivity(intent)
        }
        rvTrendingMeals.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTrendingMeals.adapter = mealAdapter

        // Search preview RV
        rvSearchResults = findViewById(R.id.rvSearchResults)
        searchAdapter = SearchMealAdapter(mutableListOf()) { meal ->
            val intent = Intent(this, RecipeDetails::class.java)
            intent.putExtra(RecipeDetails.EXTRA_MEAL_ID, meal.idMeal)
            startActivity(intent)
        }
        rvSearchResults.layoutManager = GridLayoutManager(this, 2)
        rvSearchResults.isNestedScrollingEnabled = false
        rvSearchResults.setHasFixedSize(false)
        rvSearchResults.adapter = searchAdapter
        rvSearchResults.visibility = View.GONE

        // Ingredients RV (clickable)
        rvIngredients = findViewById(R.id.rvIngredients)
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
        ingredientAdapter = IngredientAdapter(ingredients) { ing ->
            runIngredientSearch(ing.name)
        }
        rvIngredients.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvIngredients.adapter = ingredientAdapter

        // Initial UI
        showTrendingUI()

        // A–Z footer click
        setupAlphaBar()

        // Search
        setupSearchBackend()

        // View All
        viewAllText.setOnClickListener {
            if (lastResults.isNotEmpty()) {
                val intent = Intent(this, SearchResultsActivity::class.java)
                intent.putExtra(SearchResultsActivity.EXTRA_QUERY, lastQueryTitle)
                intent.putParcelableArrayListExtra(SearchResultsActivity.EXTRA_MEALS, lastResults)
                startActivity(intent)
            }
        }

        // Sign In
        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Profile (drawer)
        profileBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
            loadDrawerUserInfo()
        }

        // Filter button
        val filterBtn = findViewById<MaterialButton>(R.id.filterBtn)
        filterBtn.setOnClickListener {
            val sheet = MealFilterBottomSheet()
            sheet.setFilterListener(this)
            sheet.show(supportFragmentManager, MealFilterBottomSheet.TAG)
        }

        // Drawer menu
        rightDrawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }

                R.id.menu_logout -> {
                    auth.signOut()
                    drawerLayout.closeDrawer(GravityCompat.END)
                    updateTopBar()

                    val header = rightDrawer.getHeaderView(0)
                    header.findViewById<TextView>(R.id.drawerName).text = "Guest"
                    header.findViewById<TextView>(R.id.drawerEmail).text = ""
                    true
                }

                else -> false
            }
        }

        // Trending meals stream
        lifecycleScope.launch {
            viewModel.meals.collect { meals ->
                latestTrendingMeals = meals
                if (searchEditText.text.isNullOrBlank()) {
                    showTrendingUI()
                    mealAdapter.updateMeals(meals)
                    clearViewAllPayload()
                }
            }
        }
        viewModel.loadRandomMeals()

        // Footer year
        val year = Calendar.getInstance().get(Calendar.YEAR)
        findViewById<TextView>(R.id.footer_year).text =
            "© $year MealMatch. All rights reserved."

        applyNavbarStyle(scrolled = false)
    }

    override fun onStart() {
        super.onStart()
        updateTopBar()
    }

    // =========================
    // Filter callback (from BottomSheet)
    // =========================
    override fun onFilterSelected(categories: List<String>, areas: List<String>) {
        runFilter(categories, areas)
    }

    private fun runFilter(categories: List<String>, areas: List<String>) {
        job?.cancel()
        job = lifecycleScope.launch {
            val results = repo.filterMulti(categories, areas)

            lastQueryTitle = buildFilterTitle(categories, areas)
            lastResults = ArrayList(results)

            showSearchUI(lastQueryTitle, results.size)
            searchAdapter.setMeals(results.take(HOME_PREVIEW_LIMIT))

            scrollToResults()
        }
    }

    private fun buildFilterTitle(categories: List<String>, areas: List<String>): String {
        val c = categories.joinToString(", ")
        val a = areas.joinToString(", ")

        return when {
            categories.isNotEmpty() && areas.isNotEmpty() -> "Filtered: $c • $a"
            categories.isNotEmpty() -> "Filtered: $c"
            areas.isNotEmpty() -> "Filtered: $a"
            else -> "Filtered Results"
        }
    }

    // =========================
    // Search (typed)
    // =========================
    private fun setupSearchBackend() {
        searchEditText.addTextChangedListener { editable ->
            val q = editable?.toString().orEmpty().trim()

            job?.cancel()
            job = lifecycleScope.launch {
                delay(400)

                if (q.isBlank()) {
                    showTrendingUI()
                    mealAdapter.updateMeals(latestTrendingMeals)
                    rvSearchResults.visibility = View.GONE
                    searchAdapter.setMeals(emptyList())
                    viewAllText.visibility = View.GONE
                    clearViewAllPayload()
                    return@launch
                }

                val results = repo.searchAll(q)

                lastQueryTitle = "Search results for \"$q\""
                lastResults = ArrayList(results)

                showSearchUI(lastQueryTitle, results.size)
                searchAdapter.setMeals(results.take(HOME_PREVIEW_LIMIT))

                scrollToResults()
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            actionId == EditorInfo.IME_ACTION_SEARCH
        }
    }

    // =========================
    // A–Z Footer (click)
    // =========================
    private fun setupAlphaBar() {
        val alphaBar = findViewById<View>(R.id.alphaBar) as? ViewGroup ?: return

        for (i in 0 until alphaBar.childCount) {
            val child = alphaBar.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    val letter = (child.tag as? String) ?: child.text.toString()
                    runAlphaSearch(letter)
                }
            }
        }
    }

    private fun runAlphaSearch(letter: String) {
        val q = letter.trim()
        if (q.isBlank()) return

        // This triggers normal search flow too (good UX)
        searchEditText.setText(q)
    }

    // =========================
    // Ingredient click
    // =========================
    private fun runIngredientSearch(ingredientName: String) {
        val q = ingredientName.trim()
        if (q.isBlank()) return

        // This triggers normal search flow too (good UX)
        searchEditText.setText(q)
    }

    // =========================
    // UI state
    // =========================
    private fun showTrendingUI() {
        trendingTitle.text = "Trending Meals"
        rvTrendingMeals.visibility = View.VISIBLE
        rvSearchResults.visibility = View.GONE
        viewAllText.visibility = View.GONE
    }

    private fun showSearchUI(title: String, totalCount: Int) {
        trendingTitle.text = title
        rvTrendingMeals.visibility = View.GONE
        rvSearchResults.visibility = View.VISIBLE
        viewAllText.visibility = if (totalCount > HOME_PREVIEW_LIMIT) View.VISIBLE else View.GONE
        viewAllText.text = "View All"
    }

    private fun scrollToResults() {
        scrollView.post {
            val anchor = findViewById<View>(R.id.searchRow)
            scrollView.smoothScrollTo(0, anchor.top)
        }
    }

    private fun clearViewAllPayload() {
        lastQueryTitle = ""
        lastResults = arrayListOf()
    }

    // =========================
    // Top bar + drawer
    // =========================
    private fun updateTopBar() {
        val loggedIn = auth.currentUser != null
        signInBtn.visibility = if (loggedIn) View.GONE else View.VISIBLE
        profileBtn.visibility = if (loggedIn) View.VISIBLE else View.GONE
    }

    private fun loadDrawerUserInfo() {
        val user = auth.currentUser ?: return

        val header = rightDrawer.getHeaderView(0)
        val nameTv = header.findViewById<TextView>(R.id.drawerName)
        val emailTv = header.findViewById<TextView>(R.id.drawerEmail)

        emailTv.text = user.email ?: "No email"

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                nameTv.text = doc.getString("name") ?: "User"
            }
            .addOnFailureListener {
                nameTv.text = "User"
            }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    // =========================
    // Navbar scroll
    // =========================
    private fun setupNavbarScrollBehavior() {
        val thresholdPx = dpToPx(8)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val shouldBeScrolled = scrollY > thresholdPx
            if (shouldBeScrolled != isScrolledStyle) {
                isScrolledStyle = shouldBeScrolled
                applyNavbarStyle(scrolled = shouldBeScrolled)
            }
        }
    }

    private fun applyNavbarStyle(scrolled: Boolean) {
        if (scrolled) {
            topNavbar.setBackgroundColor(Color.WHITE)
            tvMeal.setTextColor(Color.BLACK)

            signInBtn.setTextColor(Color.WHITE)
            signInBtn.iconTint = ColorStateList.valueOf(Color.WHITE)
            signInBtn.strokeColor = ColorStateList.valueOf(Color.BLACK)
            signInBtn.backgroundTintList = ColorStateList.valueOf(Color.BLACK)

            profileBtn.setImageResource(R.drawable.ic_profile_black)
            profileBtn.strokeColor = ColorStateList.valueOf(Color.BLACK)
            profileBtn.strokeWidth = dpToPx(1).toFloat()
        } else {
            topNavbar.setBackgroundColor(Color.TRANSPARENT)
            tvMeal.setTextColor(Color.WHITE)

            signInBtn.setTextColor(Color.WHITE)
            signInBtn.iconTint = ColorStateList.valueOf(Color.WHITE)
            signInBtn.strokeColor = ColorStateList.valueOf(Color.WHITE)
            signInBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.signin_bg_states)

            profileBtn.setImageResource(R.drawable.ic_profile_white)
            profileBtn.strokeColor = ColorStateList.valueOf(Color.WHITE)
            profileBtn.strokeWidth = dpToPx(1).toFloat()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}