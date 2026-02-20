package com.example.mealmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SearchMealAdapter(
    private val meals: MutableList<Meal>,
    private val onClick: (Meal) -> Unit
) : RecyclerView.Adapter<SearchMealAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMeal: ImageView = itemView.findViewById(R.id.imgMeal)
        val txtMealName: TextView = itemView.findViewById(R.id.txtMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.txtMealName.text = meal.strMeal

        Picasso.get()
            .load(meal.strMealThumb)
            .fit()
            .centerCrop()
            .into(holder.imgMeal)

        holder.itemView.setOnClickListener { onClick(meal) }
    }

    override fun getItemCount(): Int = meals.size

    fun setMeals(newMeals: List<Meal>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }

    fun appendMeals(more: List<Meal>) {
        val start = meals.size
        meals.addAll(more)
        notifyItemRangeInserted(start, more.size)
    }
}