package com.jaynes.maxtv.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.ActivitySearchBinding
import com.jaynes.maxtv.ui.home.ChannelsGridAdapter
import com.jaynes.maxtv.ui.player.PlayerActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tokenManager: TokenManager
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        binding.ivBack.setOnClickListener { finish() }

        binding.rvResults.layoutManager = GridLayoutManager(this, 3)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString().trim()
                if (query.length >= 2) {
                    searchJob = lifecycleScope.launch {
                        delay(400) // Debounce
                        doSearch(query)
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private suspend fun doSearch(query: String) {
        try {
            binding.progressBar.visibility = View.VISIBLE
            val token = tokenManager.getToken() ?: return
            val res = RetrofitClient.api.search("Bearer $token", query)
            if (res.isSuccessful && res.body()?.success == true) {
                val results = res.body()!!.results
                binding.tvResultCount.text = "Matokeo: ${results.size}"
                binding.rvResults.adapter = ChannelsGridAdapter(results) { ch ->
                    PlayerActivity.start(this, ch.name, ch.url, ch.key, ch.type, ch.logoFinal)
                }
            }
        } catch (e: Exception) {
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }
}
