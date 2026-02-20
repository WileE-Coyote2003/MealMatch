package com.example.mealmatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecipeIngredientAdapter(
    private val items: List<RecipeIngredient>
) : RecyclerView.Adapter<RecipeIngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeIngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_ingredient, parent, false)
        return RecipeIngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeIngredientViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.measure.text = item.measure
        holder.img.setImageResource(item.imageRes)
    }

    override fun getItemCount(): Int = items.size
}