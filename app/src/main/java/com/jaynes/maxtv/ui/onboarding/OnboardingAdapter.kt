package com.jaynes.maxtv.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaynes.maxtv.databinding.ItemOnboardingBinding

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.tvIcon.text  = page.icon
        holder.binding.tvTitle.text = page.title
        holder.binding.tvDesc.text  = page.desc
    }

    override fun getItemCount() = pages.size
}
