package com.example.mealmatch

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: MealViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter
    private lateinit var ingredientAdapter: IngredientAdapter

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var signInBtn: MaterialButton
    private lateinit var profileBtn: ShapeableImageView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rightDrawer: NavigationView

    // ===== Navbar scroll behavior views =====
    private lateinit var scrollView: NestedScrollView
    private lateinit var topNavbar: ConstraintLayout
    private lateinit var tvMeal: TextView
    private lateinit var tvMatch: TextView

    // track current state to avoid re-setting colors every pixel
    private var isScrolledStyle = false

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

        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        profileBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
            loadDrawerUserInfo()
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

        // Footer year
        val year = Calendar.getInstance().get(Calendar.YEAR)
        findViewById<TextView>(R.id.footer_year).text =
            "© $year MealMatch. All rights reserved."

        // Apply correct navbar style immediately (top at start)
        applyNavbarStyle(scrolled = false)
    }

    override fun onStart() {
        super.onStart()
        updateTopBar()
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
    // NAVBAR SCROLL IMPLEMENTATION (transparent at top, white when scroll)
    // ==========================================================
    private fun setupNavbarScrollBehavior() {
        val thresholdPx = dpToPx(8) // change when scrollY > 8dp

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
            // White navbar
            topNavbar.setBackgroundColor(Color.WHITE)

            // Logo text color
            tvMeal.setTextColor(Color.BLACK)
            // tvMatch stays orange as in xml

            // Sign in button (black outline on white)
            signInBtn.setTextColor(Color.WHITE)
            signInBtn.iconTint = ColorStateList.valueOf(Color.WHITE)
            signInBtn.strokeColor = ColorStateList.valueOf(Color.BLACK)
            signInBtn.backgroundTintList = ColorStateList.valueOf(Color.BLACK)

            // Profile icon swap to black (you must have this drawable)
            profileBtn.setImageResource(R.drawable.ic_profile_black)
            profileBtn.strokeColor = ColorStateList.valueOf(Color.BLACK)
            profileBtn.strokeWidth = dpToPx(1).toFloat()

        } else {
            // Transparent navbar (on top of header image)
            topNavbar.setBackgroundColor(Color.TRANSPARENT)

            // Logo text color
            tvMeal.setTextColor(Color.WHITE)

            // Sign in button (your original)
            signInBtn.setTextColor(Color.WHITE)
            signInBtn.iconTint = ColorStateList.valueOf(Color.WHITE)
            signInBtn.strokeColor = ColorStateList.valueOf(Color.WHITE)
            signInBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.signin_bg_states)

            // Profile icon swap back to white
            profileBtn.setImageResource(R.drawable.ic_profile_white)
            profileBtn.strokeColor = ColorStateList.valueOf(Color.WHITE)
            profileBtn.strokeWidth = dpToPx(1).toFloat()

        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}