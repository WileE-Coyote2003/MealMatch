package com.example.mealmatch

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imgIngredient: ImageView = itemView.findViewById(R.id.imgIngredient)
    val txtIngredientName: TextView = itemView.findViewById(R.id.txtIngredientName)
}
