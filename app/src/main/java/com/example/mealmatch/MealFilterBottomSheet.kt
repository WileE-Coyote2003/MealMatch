package com.example.mealmatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class MealFilterBottomSheet : BottomSheetDialogFragment() {

    interface FilterListener {
        fun onFilterSelected(categories: List<String>, areas: List<String>)
    }

    private var listener: FilterListener? = null

    fun setFilterListener(l: FilterListener) {
        listener = l
    }

    private lateinit var tvCategorySelected: TextView
    private lateinit var tvAreaSelected: TextView
    private lateinit var btnClearAll: Button
    private lateinit var btnShowResults: Button

    private var selectedCountC = 0
    private var selectedCountA = 0

    private val selectedCategoryMap = mutableMapOf<Int, Boolean>()
    private val selectedAreaMap = mutableMapOf<Int, Boolean>()

    private lateinit var categoryIds: List<Int>
    private lateinit var areaIds: List<Int>

    // IMPORTANT: map your UI IDs to API values (because your UI text doesn't match API)
    private val areaIdToApiValue: Map<Int, String> = mapOf(
        R.id.a_uk to "British",
        R.id.a_us to "American",
        R.id.a_france to "French",
        R.id.a_canada to "Canadian",
        R.id.a_jamaica to "Jamaican",
        R.id.a_china to "Chinese",
        R.id.a_netherlands to "Dutch",
        R.id.a_egypt to "Egyptian",
        R.id.a_greece to "Greek",
        R.id.a_india to "Indian",
        R.id.a_ireland to "Irish",
        R.id.a_italy to "Italian",
        R.id.a_japan to "Japanese",
        // R.id.a_skn -> NOT in your API list you pasted. If you keep it, it will return empty.
        R.id.a_malaysia to "Malaysian",
        R.id.a_mexico to "Mexican",
        R.id.a_morocco to "Moroccan",
        R.id.a_croatia to "Croatian",
        R.id.a_norway to "Norwegian",
        R.id.a_portugal to "Portuguese",
        R.id.a_russia to "Russian",
        R.id.a_argentina to "Argentinian",
        R.id.a_spain to "Spanish",
        R.id.a_slovakia to "Slovakian",
        R.id.a_thailand to "Thai",
        R.id.a_saudi to "Saudi Arabian",
        R.id.a_vietnam to "Vietnamese",
        R.id.a_turkey to "Turkish",
        R.id.a_syria to "Syrian",
        R.id.a_algeria to "Algerian",
        R.id.a_tunisia to "Tunisian",
        R.id.a_poland to "Polish",
        R.id.a_philippines to "Filipino",
        R.id.a_ukraine to "Ukrainian",
        R.id.a_uruguay to "Uruguayan",
        R.id.a_australia to "Australian",
        R.id.a_venezuela to "Venezulan" // note: API spelling is "Venezulan"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_meal_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCategorySelected = view.findViewById(R.id.tv_category_selected)
        tvAreaSelected = view.findViewById(R.id.tv_area_selected)
        btnClearAll = view.findViewById(R.id.btn_clear_all)
        btnShowResults = view.findViewById(R.id.btn_show_results)

        categoryIds = listOf(
            R.id.c_beef,
            R.id.c_chicken,
            R.id.c_dessert,
            R.id.c_lamb,
            R.id.c_miscellaneous,
            R.id.c_pasta,
            R.id.c_pork,
            R.id.c_seafood,
            R.id.c_side,
            R.id.c_starter,
            R.id.c_vegan,
            R.id.c_vegetarian,
            R.id.c_breakfast,
            R.id.c_goat
        )

        areaIds = listOf(
            R.id.a_uk,
            R.id.a_us,
            R.id.a_france,
            R.id.a_canada,
            R.id.a_jamaica,
            R.id.a_china,
            R.id.a_netherlands,
            R.id.a_egypt,
            R.id.a_greece,
            R.id.a_india,
            R.id.a_ireland,
            R.id.a_italy,
            R.id.a_japan,
            R.id.a_malaysia,
            R.id.a_mexico,
            R.id.a_morocco,
            R.id.a_croatia,
            R.id.a_norway,
            R.id.a_portugal,
            R.id.a_russia,
            R.id.a_argentina,
            R.id.a_spain,
            R.id.a_slovakia,
            R.id.a_thailand,
            R.id.a_saudi,
            R.id.a_vietnam,
            R.id.a_turkey,
            R.id.a_syria,
            R.id.a_algeria,
            R.id.a_tunisia,
            R.id.a_poland,
            R.id.a_philippines,
            R.id.a_ukraine,
            R.id.a_uruguay,
            R.id.a_australia,
            R.id.a_venezuela
        )

        setupToggleClicks(
            root = view,
            ids = categoryIds,
            stateMap = selectedCategoryMap,
            onSelected = { selectedCountC++; updateSelectedTextC() },
            onUnselected = { selectedCountC--; updateSelectedTextC() }
        )

        setupToggleClicks(
            root = view,
            ids = areaIds,
            stateMap = selectedAreaMap,
            onSelected = { selectedCountA++; updateSelectedTextA() },
            onUnselected = { selectedCountA--; updateSelectedTextA() }
        )

        updateSelectedTextC()
        updateSelectedTextA()

        btnClearAll.setOnClickListener { clearAllSelections(view) }

        view.findViewById<View>(R.id.btnClose).setOnClickListener { dismiss() }

        btnShowResults.setOnClickListener {
            val selectedCategories = getSelectedCategories(view)
            val selectedAreas = getSelectedAreas()

            // Send back to MainActivity
            listener?.onFilterSelected(selectedCategories, selectedAreas)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val sheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    private fun setupToggleClicks(
        root: View,
        ids: List<Int>,
        stateMap: MutableMap<Int, Boolean>,
        onSelected: () -> Unit,
        onUnselected: () -> Unit
    ) {
        for (id in ids) {
            val v = root.findViewById<View>(id)
            stateMap[id] = false
            v.isSelected = false

            v.setOnClickListener {
                val currentlySelected = stateMap[id] ?: false
                if (!currentlySelected) {
                    stateMap[id] = true
                    v.isSelected = true
                    onSelected()
                } else {
                    stateMap[id] = false
                    v.isSelected = false
                    onUnselected()
                }
            }
        }
    }

    private fun clearAllSelections(root: View) {
        for (id in categoryIds) {
            root.findViewById<View>(id).isSelected = false
            selectedCategoryMap[id] = false
        }
        for (id in areaIds) {
            root.findViewById<View>(id).isSelected = false
            selectedAreaMap[id] = false
        }

        selectedCountC = 0
        selectedCountA = 0
        updateSelectedTextC()
        updateSelectedTextA()
    }

    private fun updateSelectedTextC() {
        tvCategorySelected.text = "$selectedCountC selected"
    }

    private fun updateSelectedTextA() {
        tvAreaSelected.text = "$selectedCountA selected"
    }

    private fun getSelectedCategories(root: View): List<String> {
        val out = mutableListOf<String>()
        for (id in categoryIds) {
            if (selectedCategoryMap[id] == true) {
                val btn = root.findViewById<View>(id)
                val text = when (btn) {
                    is MaterialButton -> btn.text?.toString().orEmpty()
                    else -> ""
                }.trim()
                if (text.isNotEmpty()) out.add(text)
            }
        }
        return out
    }

    private fun getSelectedAreas(): List<String> {
        val out = mutableListOf<String>()
        for (id in areaIds) {
            if (selectedAreaMap[id] == true) {
                val apiValue = areaIdToApiValue[id]
                if (!apiValue.isNullOrBlank()) out.add(apiValue)
            }
        }
        return out
    }

    companion object {
        const val TAG = "MealFilterBottomSheet"
    }
}