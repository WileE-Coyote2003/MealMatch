package com.example.mealmatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class IngredientAdapter(
    private val ingredients: List<Ingredient>,
    private val onClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.txtIngredientName.text = ingredient.name

        Picasso.get()
            .load(ingredient.imageUrl)
            .fit()
            .centerCrop()
            .into(holder.imgIngredient)

        holder.itemView.setOnClickListener {
            onClick(ingredient)
        }
    }

    override fun getItemCount(): Int = ingredients.size
}