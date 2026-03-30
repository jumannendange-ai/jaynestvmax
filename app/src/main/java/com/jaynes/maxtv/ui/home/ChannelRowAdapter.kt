package com.jaynes.maxtv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jaynes.maxtv.data.model.Channel
import com.jaynes.maxtv.databinding.ItemChannelBinding

class ChannelRowAdapter(
    private val channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelRowAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channels[position]
        holder.binding.tvChannelName.text = channel.name

        // Load logo
        if (!channel.logoFinal.isNullOrEmpty()) {
            Glide.with(holder.itemView)
                .load(channel.logoFinal)
                .placeholder(com.jaynes.maxtv.R.drawable.ic_channel_placeholder)
                .into(holder.binding.ivChannelLogo)
        }

        holder.binding.tvLiveBadge.visibility =
            if (channel.isLive) android.view.View.VISIBLE else android.view.View.GONE

        holder.itemView.setOnClickListener { onClick(channel) }
    }

    override fun getItemCount() = channels.size
}
