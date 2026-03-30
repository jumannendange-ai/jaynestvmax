package com.jaynes.maxtv.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.FragmentFavoritesBinding
import com.jaynes.maxtv.ui.home.ChannelsGridAdapter
import com.jaynes.maxtv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())
        loadFavorites()
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val res = RetrofitClient.api.getFavorites("Bearer $token")
                if (res.isSuccessful && res.body()?.success == true) {
                    val favs = res.body()!!.favorites
                    if (favs.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvFavorites.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvFavorites.visibility = View.VISIBLE
                        binding.rvFavorites.layoutManager = GridLayoutManager(requireContext(), 3)
                        binding.rvFavorites.adapter = ChannelsGridAdapter(favs) { ch ->
                            PlayerActivity.start(requireContext(), ch.name, ch.url, ch.key, ch.type, ch.logoFinal)
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
