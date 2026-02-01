package com.example.mealmatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MealAdapter(
    private val meals: MutableList<Meal>,
    private val onClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recomending_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
        holder.itemView.setOnClickListener { onClick(meal) }
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }
}
