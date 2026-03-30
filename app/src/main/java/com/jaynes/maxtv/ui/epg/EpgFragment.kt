package com.jaynes.maxtv.ui.epg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.FragmentEpgBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EpgFragment : Fragment() {

    private var _binding: FragmentEpgBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEpgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())
        setupDayFilter()
        loadEpg()
    }

    private fun setupDayFilter() {
        val days = listOf("LEO", "KESHO", "JUMANNE")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        binding.chipGroupDays.removeAllViews()
        days.forEachIndexed { index, day ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = day
                isCheckable = true
                isChecked = index == 0
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        cal.add(Calendar.DAY_OF_YEAR, index)
                        loadEpg(sdf.format(cal.time))
                        cal.add(Calendar.DAY_OF_YEAR, -index)
                    }
                }
            }
            binding.chipGroupDays.addView(chip)
        }
    }

    private fun loadEpg(date: String? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val token = tokenManager.getToken() ?: return@launch
                val res = RetrofitClient.api.getEpg("Bearer $token", date)
                if (res.isSuccessful && res.body()?.success == true) {
                    val epgData = res.body()!!.epg
                    val adapter = EpgAdapter(epgData)
                    binding.rvEpg.adapter = adapter
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
