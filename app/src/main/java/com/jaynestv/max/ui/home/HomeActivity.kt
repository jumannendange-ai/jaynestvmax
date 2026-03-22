package com.jaynestv.max.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.jaynestv.max.R
import com.jaynestv.max.data.api.RetrofitClient
import com.jaynestv.max.data.models.Channel
import com.jaynestv.max.data.models.Slider
import com.jaynestv.max.databinding.ActivityHomeBinding
import com.jaynestv.max.ui.account.AccountActivity
import com.jaynestv.max.ui.malipo.MalipoActivity
import com.jaynestv.max.ui.player.PlayerActivity
import com.jaynestv.max.utils.SessionManager
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    private var channelAdapter: ChannelAdapter? = null
    private var sliderAdapter: SliderAdapter? = null
    private var trialTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupRecycler()
        setupSlider()
        setupBottomNav()
        setupQuickButtons()
        loadData()
        handleAccessBanner()
    }

    private fun setupRecycler() {
        channelAdapter = ChannelAdapter(session) { channel ->
            openChannel(channel)
        }
        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = channelAdapter
        }
    }

    private fun setupSlider() {
        sliderAdapter = SliderAdapter { slider ->
            // Slider click — ignore au navigate
        }
        binding.vpSlider.adapter = sliderAdapter
        binding.dotsIndicator.attachTo(binding.vpSlider)
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener { /* already here */ }
        binding.navLive.setOnClickListener { loadCategory("live") }
        binding.navMalipo.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }
        binding.navAkaunti.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }
        // Highlight home
        binding.navHome.isSelected = true
    }

    private fun setupQuickButtons() {
        binding.btnMechi.setOnClickListener  { loadCategory("mechi") }
        binding.btnAzam.setOnClickListener   { loadCategory("azam") }
        binding.btnNBC.setOnClickListener    { loadCategory("nbc") }
        binding.btnGlobal.setOnClickListener { loadCategory("global") }
        binding.btnLocal.setOnClickListener  { loadCategory("local") }
        binding.btnMalipo.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }
    }

    private fun loadData() {
        binding.shimmerChannels.startShimmer()
        binding.shimmerChannels.visibility = View.VISIBLE
        binding.rvChannels.visibility = View.GONE

        lifecycleScope.launch {
            // Load sliders
            try {
                val resp = RetrofitClient.apiService.getCategories()
                resp.body()?.categories?.let { cats ->
                    val sliders = cats.map { cat ->
                        Slider(title = cat.name, imageUrl = cat.image, description = cat.description)
                    }
                    sliderAdapter?.submitList(sliders)
                }
            } catch (e: Exception) { /* Ignore slider error */ }

            // Load channels
            try {
                val resp = RetrofitClient.apiService.getChannels()
                val channels = resp.body()?.channels ?: emptyList()
                channelAdapter?.submitList(channels)
                binding.shimmerChannels.stopShimmer()
                binding.shimmerChannels.visibility = View.GONE
                binding.rvChannels.visibility = View.VISIBLE
                binding.errorLayout.visibility = View.GONE
            } catch (e: Exception) {
                binding.shimmerChannels.stopShimmer()
                binding.shimmerChannels.visibility = View.GONE
                binding.errorLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun loadCategory(category: String) {
        binding.shimmerChannels.startShimmer()
        binding.shimmerChannels.visibility = View.VISIBLE
        binding.rvChannels.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getChannels(category)
                channelAdapter?.submitList(resp.body()?.channels ?: emptyList())
            } catch (e: Exception) {}
            binding.shimmerChannels.stopShimmer()
            binding.shimmerChannels.visibility = View.GONE
            binding.rvChannels.visibility = View.VISIBLE
        }
    }

    private fun openChannel(channel: Channel) {
        // Check access
        if (!session.isFreeChannel(channel.name) && !session.hasAnyAccess()) {
            showPaywallDialog(channel.name)
            return
        }
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL,   channel.streamUrl)
            putExtra(PlayerActivity.EXTRA_STREAM_MPD,   channel.streamUrlMpd)
            putExtra(PlayerActivity.EXTRA_IS_LIVE,      channel.isLive)
        }
        startActivity(intent)
    }

    private fun handleAccessBanner() {
        if (session.isAdmin() || session.hasPremium()) return

        if (session.trialActive()) {
            showTrialBanner()
        } else if (!session.hasAnyAccess()) {
            // Weka trial kwa mara ya kwanza
            if (session.getTrialEnd() == 0L) {
                session.saveTrialEnd(System.currentTimeMillis() + com.jaynestv.max.utils.Constants.TRIAL_MS)
                showTrialBanner()
            } else {
                showPaywallBanner()
            }
        }
    }

    private fun showTrialBanner() {
        binding.trialBanner.visibility = View.VISIBLE
        binding.paywallBanner.visibility = View.GONE

        trialTimer = object : CountDownTimer(session.trialSecondsLeft() * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                val secs = ms / 1000
                val m = secs / 60
                val s = secs % 60
                binding.txtTrialTime.text = "${String.format("%02d", m)}:${String.format("%02d", s)}"
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

    private fun showPaywallDialog(channelName: String) {
        PaywallDialog(channelName) {
            startActivity(Intent(this, MalipoActivity::class.java))
        }.show(supportFragmentManager, "paywall")
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
            } catch (e: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        trialTimer?.cancel()
    }
}
