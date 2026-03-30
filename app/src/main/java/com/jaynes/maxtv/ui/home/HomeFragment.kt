package com.jaynes.maxtv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.model.Channel
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.FragmentHomeBinding
import com.jaynes.maxtv.ui.player.PlayerActivity
import com.jaynes.maxtv.ui.search.SearchActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private val allChannels = mutableListOf<Channel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        setupUI()
        loadChannels()
    }

    private fun setupUI() {
        // Search icon
        binding.ivSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener { loadChannels() }
    }

    private fun loadChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getAzamChannels()
                if (res.isSuccessful && res.body()?.success == true) {
                    allChannels.clear()
                    allChannels.addAll(res.body()!!.channels)
                    displayChannels()
                }
            } catch (e: Exception) {
                // Offline — onyesha cached
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun displayChannels() {
        val sports = allChannels.filter {
            it.category.contains("SPORT", ignoreCase = true) ||
            it.category.contains("PREMIER", ignoreCase = true)
        }
        val azam = allChannels.filter { it.name.contains("Azam", ignoreCase = true) }
        val music = allChannels.filter { it.category.contains("MUSIC", ignoreCase = true) }

        // Hero banner — channel ya kwanza ya sports
        sports.firstOrNull()?.let { setupHero(it) }

        // Sports row
        setupChannelRow(binding.rvSports, sports) { openPlayer(it) }

        // Azam row
        setupChannelRow(binding.rvAzam, azam) { openPlayer(it) }

        // Music row
        setupChannelRow(binding.rvMusic, music) { openPlayer(it) }

        // All channels row
        setupChannelRow(binding.rvAll, allChannels) { openPlayer(it) }
    }

    private fun setupHero(channel: Channel) {
        binding.tvHeroChannel.text = channel.name
        binding.btnHeroPlay.setOnClickListener { openPlayer(channel) }
    }

    private fun setupChannelRow(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        channels: List<Channel>,
        onClick: (Channel) -> Unit
    ) {
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        recyclerView.adapter = ChannelRowAdapter(channels, onClick)
    }

    private fun openPlayer(channel: Channel) {
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.url)
            putExtra(PlayerActivity.EXTRA_DRM_KEY, channel.key)
            putExtra(PlayerActivity.EXTRA_STREAM_TYPE, channel.type)
            putExtra(PlayerActivity.EXTRA_LOGO_URL, channel.logoFinal)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
