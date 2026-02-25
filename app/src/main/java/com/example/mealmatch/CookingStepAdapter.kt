package com.example.mealmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CookingStepAdapter(
    private val steps: List<CookingSteps>
) : RecyclerView.Adapter<CookingStepAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val number: TextView = itemView.findViewById(R.id.tvStepNumber)
        val desc: TextView = itemView.findViewById(R.id.tvStepDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cooking_step, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val step = steps[position]
        holder.number.text = step.stepNumber.toString()
        holder.desc.text = step.description

    }

    override fun getItemCount(): Int = steps.size
}