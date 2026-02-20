package com.example.mealmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CookingStepAdapter(
    private val items: List<CookingSteps>
) : RecyclerView.Adapter<CookingStepAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvStepNumber)
        val tvTitle: TextView = itemView.findViewById(R.id.tvStepTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvStepDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cooking_step, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNumber.text = item.number.toString()
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.desc
    }

    override fun getItemCount(): Int = items.size
}