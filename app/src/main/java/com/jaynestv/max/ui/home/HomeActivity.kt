package com.jaynestv.max.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jaynestv.max.data.api.RetrofitClient
import com.jaynestv.max.data.models.Channel
import com.jaynestv.max.data.models.Slider
import com.jaynestv.max.databinding.ActivityHomeBinding
import com.jaynestv.max.ui.account.AccountActivity
import com.jaynestv.max.ui.malipo.MalipoActivity
import com.jaynestv.max.ui.player.PlayerActivity
import com.jaynestv.max.utils.Constants
import com.jaynestv.max.utils.SessionManager
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    private var channelAdapter: ChannelAdapter? = null
    private var sliderAdapter: SliderAdapter? = null
    private var trialTimer: CountDownTimer? = null
    private var currentSource = Constants.SOURCE_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupRecycler()
        setupSlider()
        setupBottomNav()
        setupQuickButtons()
        loadChannels(Constants.SOURCE_ALL)
        loadSliders()
        handleAccessBanner()
        checkMaintenance()
    }

    private fun setupRecycler() {
        channelAdapter = ChannelAdapter(session) { channel -> openChannel(channel) }
        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = channelAdapter
        }
        binding.btnRetryLoad.setOnClickListener { loadChannels(currentSource) }
    }

    private fun setupSlider() {
        sliderAdapter = SliderAdapter {}
        binding.vpSlider.adapter = sliderAdapter
        binding.dotsIndicator.attachTo(binding.vpSlider)
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener { loadChannels(Constants.SOURCE_ALL) }
        binding.navLive.setOnClickListener { loadChannels(Constants.SOURCE_NBC) }
        binding.navMalipo.setOnClickListener { startActivity(Intent(this, MalipoActivity::class.java)) }
        binding.navAkaunti.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
    }

    private fun setupQuickButtons() {
        binding.btnMechi.setOnClickListener  { loadChannels(Constants.SOURCE_NBC) }
        binding.btnAzam.setOnClickListener   { loadChannels(Constants.SOURCE_AZAM) }
        binding.btnNBC.setOnClickListener    { loadChannels(Constants.SOURCE_NBC) }
        binding.btnGlobal.setOnClickListener { loadChannels(Constants.SOURCE_GLOBAL) }
        binding.btnLocal.setOnClickListener  { loadChannels(Constants.SOURCE_LOCAL) }
        binding.btnMalipo.setOnClickListener { startActivity(Intent(this, MalipoActivity::class.java)) }

        // Search
        binding.inputSearch.setOnEditorActionListener { v, _, _ ->
            val q = v.text.toString().trim()
            if (q.isNotEmpty()) searchChannels(q)
            true
        }
    }

    private fun loadChannels(source: String) {
        currentSource = source
        showLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getChannels(source = source)
                val channels = resp.body()?.channels ?: emptyList()
                channelAdapter?.submitList(channels)
                showLoading(false)
                binding.errorLayout.visibility = View.GONE

                // Load slider images kutoka channels
                if (source == Constants.SOURCE_ALL) {
                    val sliders = channels.take(5).map { ch ->
                        Slider(title = ch.name, imageUrl = ch.logo, description = ch.category)
                    }
                    sliderAdapter?.submitList(sliders)
                }
            } catch (e: Exception) {
                showLoading(false)
                binding.errorLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun loadSliders() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getCategories()
                val sliders = resp.body()?.categories?.take(5)?.map { cat ->
                    Slider(title = cat.name, imageUrl = cat.image, description = cat.description)
                } ?: emptyList()
                if (sliders.isNotEmpty()) sliderAdapter?.submitList(sliders)
            } catch (e: Exception) { /* Ignore */ }
        }
    }

    private fun searchChannels(query: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getChannels(query = query)
                channelAdapter?.submitList(resp.body()?.channels ?: emptyList())
                showLoading(false)
            } catch (e: Exception) { showLoading(false) }
        }
    }

    private fun openChannel(channel: Channel) {
        if (!session.isFreeChannel(channel.name) && !session.hasAnyAccess()) {
            PaywallDialog(channel.name) {
                startActivity(Intent(this, MalipoActivity::class.java))
            }.show(supportFragmentManager, "paywall")
            return
        }
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL,   channel.streamUrl)
            putExtra(PlayerActivity.EXTRA_STREAM_MPD,   channel.streamUrlMpd)
            putExtra(PlayerActivity.EXTRA_IS_LIVE,      channel.isLive)
            putExtra(PlayerActivity.EXTRA_CLEARKEY_KID, channel.clearkeyKid)
            putExtra(PlayerActivity.EXTRA_CLEARKEY_KEY, channel.clearkeyKey)
        }
        startActivity(intent)
    }

    private fun handleAccessBanner() {
        if (session.isAdmin() || session.hasPremium()) return
        if (session.trialActive()) {
            showTrialBanner()
        } else if (session.getTrialEnd() == 0L) {
            session.saveTrialEnd(System.currentTimeMillis() + Constants.TRIAL_MS)
            showTrialBanner()
        } else {
            showPaywallBanner()
        }
    }

    private fun showTrialBanner() {
        binding.trialBanner.visibility = View.VISIBLE
        binding.paywallBanner.visibility = View.GONE
        trialTimer = object : CountDownTimer(session.trialSecondsLeft() * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                val s = ms / 1000
                binding.txtTrialTime.text = "%02d:%02d".format(s / 60, s % 60)
            }
            override fun onFinish() {
                binding.trialBanner.visibility = View.GONE
                showPaywallBanner()
            }
        }.start()
        binding.btnTrialUpgrade.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }
    }

    private fun showPaywallBanner() {
        binding.paywallBanner.visibility = View.VISIBLE
        binding.trialBanner.visibility = View.GONE
        binding.btnPaywallLipa.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }
    }

    private fun showLoading(show: Boolean) {
        binding.shimmerChannels.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvChannels.visibility      = if (show) View.GONE    else View.VISIBLE
        if (show) binding.shimmerChannels.startShimmer()
        else      binding.shimmerChannels.stopShimmer()
    }

    private fun checkMaintenance() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.checkMaintenance()
                if (resp.body()?.maintenance == true && !session.isAdmin()) {
                    // Onyesha maintenance screen
                    showMaintenanceOverlay()
                }
            } catch (e: Exception) { /* Ignore */ }
        }
    }

    private fun showMaintenanceOverlay() {
        binding.paywallBanner.visibility = View.GONE
        binding.trialBanner.visibility = View.GONE
        // Simple overlay
        android.app.AlertDialog.Builder(this)
            .setTitle("🔧 Matengenezo")
            .setMessage("App imefungwa kwa muda kwa ajili ya matengenezo.\nTutarudi hivi karibuni!")
            .setCancelable(false)
            .setPositiveButton("Sawa") { _, _ -> finish() }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh subscription baada ya kurudi kutoka Malipo
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.checkSubscription(email = session.getEmail())
                resp.body()?.let {
                    if (it.active) {
                        session.saveSubscription("premium", it.endDate)
                        binding.trialBanner.visibility = View.GONE
                        binding.paywallBanner.visibility = View.GONE
                        trialTimer?.cancel()
                    }
                }
            } catch (e: Exception) { /* Ignore */ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        trialTimer?.cancel()
    }
}
