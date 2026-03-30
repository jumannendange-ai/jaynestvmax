package com.jaynes.maxtv.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaynes.maxtv.databinding.ItemQualityBinding

class QualityAdapter(
    private val qualities: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<QualityAdapter.ViewHolder>() {

    private var selectedIndex = 0

    inner class ViewHolder(val binding: ItemQualityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemQualityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quality = qualities[position]
        holder.binding.tvQuality.text = quality

        val selected = position == selectedIndex
        holder.binding.tvQuality.setTextColor(
            if (selected) android.graphics.Color.parseColor("#E50914")
            else android.graphics.Color.parseColor("#AAAAAA")
        )

        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onSelect(quality)
        }
    }

    override fun getItemCount() = qualities.size
}
