package com.jaynes.maxtv.ui.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaynes.maxtv.data.model.Plan
import com.jaynes.maxtv.databinding.ItemPlanBinding

class PlansAdapter(
    private val plans: List<Plan>,
    private val onSelect: (Plan) -> Unit
) : RecyclerView.Adapter<PlansAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(val binding: ItemPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = plans[position]
        holder.binding.tvPlanName.text  = plan.name.uppercase()
        holder.binding.tvPrice.text     = "TZS ${plan.priceMonthly}/mwezi"
        holder.binding.tvFeatures.text  = plan.features.joinToString(" · ")
        holder.binding.tvDevices.text   = "Vifaa: ${plan.maxDevices}"

        val isSelected = position == selectedPosition
        holder.binding.cardPlan.strokeWidth = if (isSelected) 3 else 1
        holder.binding.cardPlan.strokeColor = if (isSelected)
            android.graphics.Color.parseColor("#E50914")
        else
            android.graphics.Color.parseColor("#333333")

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onSelect(plan)
        }
    }

    override fun getItemCount() = plans.size
}
