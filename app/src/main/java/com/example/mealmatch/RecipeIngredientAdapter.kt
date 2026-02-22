package com.example.mealmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

        // ✅ No crash when imageRes is null
        val res = item.imageRes
        if (res != null) {
            holder.img.visibility = View.VISIBLE
            holder.img.setImageResource(res)
        } else {
            // You can hide it OR show a placeholder.
            holder.img.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}