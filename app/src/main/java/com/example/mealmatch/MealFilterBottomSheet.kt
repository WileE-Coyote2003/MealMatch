package com.example.mealmatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MealFilterBottomSheet : BottomSheetDialogFragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use your existing XML
        return inflater.inflate(R.layout.activity_meal_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCategorySelected = view.findViewById(R.id.tv_category_selected)
        tvAreaSelected = view.findViewById(R.id.tv_area_selected)
        btnClearAll = view.findViewById(R.id.btn_clear_all)
        btnShowResults = view.findViewById(R.id.btn_show_results)

        updateSelectedTextC()
        updateSelectedTextA()

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
            R.id.a_skn,
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

        btnClearAll.setOnClickListener {
            clearAllSelections(view)
        }

        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        // For now: just close popup when show results clicked
        btnShowResults.setOnClickListener {
            dismiss()
        }
    }

    // ✅ IMPORTANT: Force BottomSheet expand so scroll content shows
    override fun onStart() {
        super.onStart()

        val sheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
        behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true

        // ✅ prevent dragging (no conflict with scrolling)
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

    companion object {
        const val TAG = "MealFilterBottomSheet"
    }
}