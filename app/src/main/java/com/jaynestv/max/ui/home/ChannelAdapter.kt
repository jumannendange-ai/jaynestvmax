package com.jaynestv.max.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jaynestv.max.R
import com.jaynestv.max.data.models.Channel
import com.jaynestv.max.databinding.ItemChannelBinding
import com.jaynestv.max.utils.SessionManager

class ChannelAdapter(
    private val session: SessionManager,
    private val onClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.VH>(DIFF) {

    inner class VH(val b: ItemChannelBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = getItem(position)
        val b  = holder.b

        b.txtChannelName.text = ch.name
        b.txtCategory.text    = ch.category

        // Logo
        Glide.with(b.imgLogo.context)
            .load(ch.logo)
            .placeholder(R.drawable.ic_channel_placeholder)
            .error(R.drawable.ic_channel_placeholder)
            .into(b.imgLogo)

        // LIVE badge
        b.badgeLive.visibility = if (ch.isLive) View.VISIBLE else View.GONE

        // PREMIUM / FREE badge
        val isFree = session.isFreeChannel(ch.name) || ch.isFree
        val hasAccess = session.hasAnyAccess()
        when {
            isFree -> {
                b.badgeAccess.visibility = View.VISIBLE
                b.badgeAccess.text = "BURE"
                b.badgeAccess.setBackgroundResource(R.drawable.bg_badge_free)
                b.imgLogo.alpha = 1f
            }
            !hasAccess -> {
                b.badgeAccess.visibility = View.VISIBLE
                b.badgeAccess.text = "PREMIUM"
                b.badgeAccess.setBackgroundResource(R.drawable.bg_badge_premium)
                b.imgLogo.alpha = 0.65f
            }
            else -> {
                b.badgeAccess.visibility = View.GONE
                b.imgLogo.alpha = 1f
            }
        }

        b.root.setOnClickListener { onClick(ch) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(a: Channel, b: Channel) = a.id == b.id
            override fun areContentsTheSame(a: Channel, b: Channel) = a == b
        }
    }
}
