package com.example.mealmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecipeIngredientAdapter(
    private val items: List<RecipeIngredient>
) : RecyclerView.Adapter<RecipeIngredientAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.ivIngredient)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val amount: TextView = itemView.findViewById(R.id.tvMeasure)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_ingredient, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.amount.text = item.amount

        holder.img.visibility = View.VISIBLE

        Picasso.get()
            .load(ImageUtils.ingredientImageUrl(item.name))
            .fit()
            .centerInside()
            .into(holder.img)
    }

    override fun getItemCount(): Int = items.size
}