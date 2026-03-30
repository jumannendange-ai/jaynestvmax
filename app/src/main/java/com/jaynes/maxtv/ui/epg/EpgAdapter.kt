package com.jaynes.maxtv.ui.epg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaynes.maxtv.data.model.EpgProgramme
import com.jaynes.maxtv.databinding.ItemEpgRowBinding

class EpgAdapter(private val epgData: Map<String, List<EpgProgramme>>) :
    RecyclerView.Adapter<EpgAdapter.ViewHolder>() {

    private val channels = epgData.keys.toList()

    inner class ViewHolder(val binding: ItemEpgRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemEpgRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channelName = channels[position]
        val programmes = epgData[channelName] ?: emptyList()

        holder.binding.tvChannelName.text = channelName
        holder.binding.rvProgrammes.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.binding.rvProgrammes.adapter = ProgrammeAdapter(programmes)
    }

    override fun getItemCount() = channels.size
}

class ProgrammeAdapter(private val programmes: List<EpgProgramme>) :
    RecyclerView.Adapter<ProgrammeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: com.jaynes.maxtv.databinding.ItemProgrammeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        com.jaynes.maxtv.databinding.ItemProgrammeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prog = programmes[position]
        holder.binding.tvTitle.text = prog.title
        holder.binding.tvTime.text  = prog.startTime.take(5)

        // Highlight inayocheza sasa
        val isNow = prog.isLive
        holder.itemView.alpha = if (isNow) 1f else 0.6f
    }

    override fun getItemCount() = programmes.size
}
