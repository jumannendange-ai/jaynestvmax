package com.jaynestv.max.ui.malipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaynestv.max.R
import com.jaynestv.max.data.models.PayMethod
import com.jaynestv.max.data.models.Plan
import com.jaynestv.max.databinding.ItemMethodBinding
import com.jaynestv.max.databinding.ItemPlanBinding

// ── Plan Adapter ──────────────────────────────────────────────────
class PlanAdapter(
    private val plans: List<Plan>,
    private val onSelect: (Plan) -> Unit
) : RecyclerView.Adapter<PlanAdapter.VH>() {

    private var selectedPos = 1 // Mwezi selected by default

    inner class VH(val b: ItemPlanBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = plans.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val plan = plans[position]
        val b    = holder.b
        val isSelected = position == selectedPos

        b.txtPlanName.text  = plan.nameSwahili
        b.txtPlanPrice.text = "TSh ${String.format("%,d", plan.price)}"
        b.txtDuration.text  = durationLabel(plan.durationDays)

        b.badgePopular.visibility = if (plan.isPopular) android.view.View.VISIBLE else android.view.View.GONE

        b.root.isSelected = isSelected
        b.root.setBackgroundResource(
            if (isSelected) R.drawable.bg_plan_selected else R.drawable.bg_plan_normal
        )

        b.root.setOnClickListener {
            val prev = selectedPos
            selectedPos = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onSelect(plan)
        }

        // Auto-select default
        if (isSelected) onSelect(plan)
    }

    private fun durationLabel(days: Int) = when {
        days < 30  -> "kwa siku $days"
        days < 60  -> "kwa mwezi 1"
        days < 120 -> "kwa miezi 3"
        days < 200 -> "kwa miezi 6"
        else       -> "kwa mwaka mzima"
    }
}

// ── Method Adapter ────────────────────────────────────────────────
class MethodAdapter(
    private val methods: List<PayMethod>,
    private val onSelect: (PayMethod) -> Unit
) : RecyclerView.Adapter<MethodAdapter.VH>() {

    private var selectedPos = 0

    inner class VH(val b: ItemMethodBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = methods.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = methods[position]
        val b = holder.b
        val isSelected = position == selectedPos

        b.txtMethodIcon.text = m.icon
        b.txtMethodName.text = m.name
        b.root.isSelected = isSelected
        b.root.setBackgroundResource(
            if (isSelected) R.drawable.bg_method_selected else R.drawable.bg_method_normal
        )

        b.root.setOnClickListener {
            val prev = selectedPos
            selectedPos = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onSelect(m)
        }

        if (isSelected) onSelect(m)
    }
}
