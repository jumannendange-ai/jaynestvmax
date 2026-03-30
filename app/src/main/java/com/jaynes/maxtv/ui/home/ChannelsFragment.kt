package com.jaynes.maxtv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.model.Channel
import com.jaynes.maxtv.databinding.FragmentChannelsBinding
import com.jaynes.maxtv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!
    private val channels = mutableListOf<Channel>()
    private var selectedCategory = "Zote"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategoryFilter()
        loadChannels()
    }

    private fun setupRecyclerView() {
        binding.rvChannels.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvChannels.adapter = ChannelsGridAdapter(channels) { channel ->
            PlayerActivity.start(
                requireContext(),
                channel.name,
                channel.url,
                channel.key,
                channel.type,
                channel.logoFinal
            )
        }
    }

    private fun setupCategoryFilter() {
        val categories = listOf("Zote", "SPORTS", "NEWS", "MUSIC", "KIDS", "MOVIES")
        binding.chipGroup.removeAllViews()
        categories.forEach { cat ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isChecked = cat == selectedCategory
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectedCategory = cat
                        filterChannels()
                    }
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun filterChannels() {
        val adapter = binding.rvChannels.adapter as ChannelsGridAdapter
        val filtered = if (selectedCategory == "Zote") channels
        else channels.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        adapter.updateChannels(filtered)
    }

    private fun loadChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val res = RetrofitClient.api.getAzamChannels()
                if (res.isSuccessful && res.body()?.success == true) {
                    channels.clear()
                    channels.addAll(res.body()!!.channels)
                    filterChannels()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
