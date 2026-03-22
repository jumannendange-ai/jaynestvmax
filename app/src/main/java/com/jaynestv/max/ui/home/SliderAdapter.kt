package com.jaynestv.max.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jaynestv.max.R
import com.jaynestv.max.data.models.Slider
import com.jaynestv.max.databinding.ItemSliderBinding

class SliderAdapter(
    private val onClick: (Slider) -> Unit
) : RecyclerView.Adapter<SliderAdapter.VH>() {

    private val items = mutableListOf<Slider>()

    fun submitList(list: List<Slider>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemSliderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.b.txtTitle.text       = s.title
        holder.b.txtDescription.text = s.description
        Glide.with(holder.b.imgSlider.context)
            .load(s.imageUrl)
            .placeholder(R.drawable.ic_channel_placeholder)
            .centerCrop()
            .into(holder.b.imgSlider)
        holder.b.root.setOnClickListener { onClick(s) }
    }
}
