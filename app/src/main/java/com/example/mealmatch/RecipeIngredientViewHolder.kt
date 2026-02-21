package com.example.mealmatch

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeIngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val img: ImageView = itemView.findViewById(R.id.ivIngredient)
    val name: TextView = itemView.findViewById(R.id.tvName)
    val measure: TextView = itemView.findViewById(R.id.tvMeasure)

}