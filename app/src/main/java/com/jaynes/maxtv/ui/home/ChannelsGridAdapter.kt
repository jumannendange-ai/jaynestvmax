package com.jaynes.maxtv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jaynes.maxtv.R
import com.jaynes.maxtv.data.model.Channel
import com.jaynes.maxtv.databinding.ItemChannelGridBinding

class ChannelsGridAdapter(
    private var channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelsGridAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChannelGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemChannelGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ch = channels[position]
        holder.binding.tvName.text = ch.name

        Glide.with(holder.itemView)
            .load(ch.logoFinal)
            .placeholder(R.drawable.ic_channel_placeholder)
            .into(holder.binding.ivLogo)

        holder.itemView.setOnClickListener { onClick(ch) }
    }

    override fun getItemCount() = channels.size

    fun updateChannels(newList: List<Channel>) {
        channels = newList
        notifyDataSetChanged()
    }
}
