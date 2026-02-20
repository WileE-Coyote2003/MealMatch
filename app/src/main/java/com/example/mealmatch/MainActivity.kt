package com.example.mealmatch

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
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

class MainActivity : AppCompatActivity() {

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

    // Search
    private lateinit var searchEditText: TextInputEditText
    private lateinit var trendingTitle: TextView
    private lateinit var viewAllText: TextView
    private val repo = MealRepository()
    private var searchJob: Job? = null

    // Keep last trending/random list for restore
    private var latestTrendingMeals: List<Meal> = emptyList()

    // Home preview: keep last query + full results for View All page
    private var lastQuery: String = ""
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

        // ScrollView
        scrollView = findViewById(R.id.mainContent)
        setupNavbarScrollBehavior()

        // Title + Search
        trendingTitle = findViewById(R.id.trendingTitle)
        viewAllText = findViewById(R.id.viewAllText)
        searchEditText = findViewById(R.id.searchEditText)

        // =========================
        // RecyclerViews
        // =========================

        // Trending Meals (Horizontal)
        rvTrendingMeals = findViewById(R.id.rvRecommendedMeals)
        mealAdapter = MealAdapter(mutableListOf()) { meal ->
            // TODO: Navigate to detail page using meal.idMeal
        }
        rvTrendingMeals.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTrendingMeals.adapter = mealAdapter

        // Search Results (Grid preview on home)
        rvSearchResults = findViewById(R.id.rvSearchResults)
        searchAdapter = SearchMealAdapter(mutableListOf()) { meal ->
            // TODO: Navigate to detail page using meal.idMeal
        }
        rvSearchResults.layoutManager = GridLayoutManager(this, 2)
        rvSearchResults.isNestedScrollingEnabled = false // inside NestedScrollView
        rvSearchResults.setHasFixedSize(false)
        rvSearchResults.adapter = searchAdapter
        rvSearchResults.visibility = View.GONE

        // Ingredients (Horizontal)
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
        ingredientAdapter = IngredientAdapter(ingredients)
        rvIngredients.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvIngredients.adapter = ingredientAdapter

        // Initial UI state
        showTrendingUI()

        // ✅ A–Z click listeners
        setupAlphaBar()

        // Search listeners
        setupSearchBackendTest()

        // View All -> open new page with all results (works for Search + A–Z)
        viewAllText.setOnClickListener {
            if (lastResults.isNotEmpty()) {
                val intent = Intent(this, SearchResultsActivity::class.java)
                intent.putExtra(SearchResultsActivity.EXTRA_QUERY, lastQuery)
                intent.putParcelableArrayListExtra(SearchResultsActivity.EXTRA_MEALS, lastResults)
                startActivity(intent)
            }
        }

        // Buttons
        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        profileBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
            loadDrawerUserInfo()
        }

        val filterBtn = findViewById<MaterialButton>(R.id.filterBtn)
        filterBtn.setOnClickListener {
            MealFilterBottomSheet().show(supportFragmentManager, MealFilterBottomSheet.TAG)
        }

        // Drawer menu actions
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

        // Collect trending/random meals from VM
        lifecycleScope.launch {
            viewModel.meals.collect { meals ->
                latestTrendingMeals = meals
                val q = searchEditText.text?.toString()?.trim().orEmpty()
                if (q.isBlank()) {
                    showTrendingUI()
                    mealAdapter.updateMeals(meals)

                    // when we go back to trending, clear "View All" data
                    lastQuery = ""
                    lastResults = arrayListOf()
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

    private fun showTrendingUI() {
        trendingTitle.text = "Trending Meals"
        rvTrendingMeals.visibility = View.VISIBLE
        rvSearchResults.visibility = View.GONE

        // View All is allowed to show always (your choice)
        viewAllText.visibility = View.VISIBLE
        viewAllText.text = "View All"
    }

    private fun showSearchUI(title: String, totalCount: Int) {
        trendingTitle.text = title
        rvTrendingMeals.visibility = View.GONE
        rvSearchResults.visibility = View.VISIBLE

        // show View All only if more than 6 results
        viewAllText.visibility = if (totalCount > HOME_PREVIEW_LIMIT) View.VISIBLE else View.GONE
        viewAllText.text = "View All"
    }

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
                val name = doc.getString("name") ?: "User"
                nameTv.text = name
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

    // ==========================================================
    // ✅ A–Z Footer click (reuses repo.searchAll(letter))
    // ==========================================================
    private fun setupAlphaBar() {
        val alphaBar = findViewById<View>(R.id.alphaBar) as? android.view.ViewGroup ?: return

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

        // optional: also reflect in search box
        searchEditText.setText(q)

        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                Log.d("MEAL_ALPHA", "Searching by letter: \"$q\" ...")

                val results = repo.searchAll(q)
                Log.d("MEAL_ALPHA", "Results count = ${results.size}")

                // store full results for View All page
                lastQuery = q
                lastResults = ArrayList(results)

                showSearchUI("Meals starting with \"$q\"", results.size)

                // home shows ONLY 6 items
                val preview = results.take(HOME_PREVIEW_LIMIT)
                searchAdapter.setMeals(preview)

                // scroll to results area
                scrollView.post {
                    val anchor = findViewById<View>(R.id.searchRow)
                    scrollView.smoothScrollTo(0, anchor.top)
                }

            } catch (e: Exception) {
                Log.e("MEAL_ALPHA", "Alpha search failed: ${e.message}", e)
                showSearchUI("Meals starting with \"$q\"", 0)
                searchAdapter.setMeals(emptyList())
                viewAllText.visibility = View.GONE
                lastQuery = ""
                lastResults = arrayListOf()
            }
        }
    }

    // ==========================================================
    // SEARCH (Home preview: show only 6, View All opens new Activity)
    // ==========================================================
    private fun setupSearchBackendTest() {
        searchEditText.addTextChangedListener { editable ->
            val q = editable?.toString().orEmpty().trim()

            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(400)

                if (q.isBlank()) {
                    Log.d("MEAL_SEARCH", "Query empty -> restore trending/random")
                    showTrendingUI()
                    mealAdapter.updateMeals(latestTrendingMeals)

                    // clear search preview + view all data
                    rvSearchResults.visibility = View.GONE
                    searchAdapter.setMeals(emptyList())
                    viewAllText.visibility = View.VISIBLE
                    lastQuery = ""
                    lastResults = arrayListOf()
                    return@launch
                }

                runSearchAndLog(q)
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = searchEditText.text?.toString().orEmpty().trim()
                searchJob?.cancel()

                lifecycleScope.launch {
                    if (q.isBlank()) {
                        Log.d("MEAL_SEARCH", "IME search pressed but query empty -> restore trending")
                        showTrendingUI()
                        mealAdapter.updateMeals(latestTrendingMeals)

                        rvSearchResults.visibility = View.GONE
                        searchAdapter.setMeals(emptyList())
                        viewAllText.visibility = View.VISIBLE
                        lastQuery = ""
                        lastResults = arrayListOf()
                    } else {
                        runSearchAndLog(q)
                    }
                }
                true
            } else false
        }
    }

    private suspend fun runSearchAndLog(query: String) {
        try {
            Log.d("MEAL_SEARCH", "Searching: \"$query\" ...")
            val results = repo.searchAll(query)
            Log.d("MEAL_SEARCH", "Results count = ${results.size}")

            // store full results for View All screen
            lastQuery = query
            lastResults = ArrayList(results)

            showSearchUI("Search results for \"$query\"", results.size)

            // home shows ONLY 6 items
            val preview = results.take(HOME_PREVIEW_LIMIT)
            searchAdapter.setMeals(preview)

            // scroll to results area
            scrollView.post {
                val anchor = findViewById<View>(R.id.searchRow)
                scrollView.smoothScrollTo(0, anchor.top)
            }

        } catch (e: Exception) {
            Log.e("MEAL_SEARCH", "Search failed: ${e.message}", e)
            showSearchUI("Search results for \"$query\"", 0)
            searchAdapter.setMeals(emptyList())
            viewAllText.visibility = View.GONE
            lastQuery = ""
            lastResults = arrayListOf()
        }
    }

    // ==========================================================
    // NAVBAR SCROLL IMPLEMENTATION
    // ==========================================================
    private fun setupNavbarScrollBehavior() {
        val thresholdPx = dpToPx(8)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val shouldBeScrolledStyle = scrollY > thresholdPx
            if (shouldBeScrolledStyle != isScrolledStyle) {
                isScrolledStyle = shouldBeScrolledStyle
                applyNavbarStyle(scrolled = shouldBeScrolledStyle)
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