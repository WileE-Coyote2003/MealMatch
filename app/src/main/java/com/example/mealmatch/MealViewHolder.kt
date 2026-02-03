package com.example.mealmatch
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val imgMeal: ImageView = itemView.findViewById(R.id.imgMeal)
    private val txtMealName: TextView = itemView.findViewById(R.id.txtMealName)

    fun bind(meal: Meal) {
        txtMealName.text = meal.strMeal

        Picasso.get()
            .load(meal.strMealThumb)
            .fit()
            .centerCrop()
            .into(imgMeal)
    }
}

