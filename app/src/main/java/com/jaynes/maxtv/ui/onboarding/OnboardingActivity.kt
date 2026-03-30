package com.jaynes.maxtv.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.ActivityOnboardingBinding
import com.jaynes.maxtv.ui.auth.AuthActivity
import kotlinx.coroutines.launch

data class OnboardingPage(val icon: String, val title: String, val desc: String)

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var tokenManager: TokenManager

    private val pages = listOf(
        OnboardingPage("🎬", "KARIBU JAYNES MAX TV", "Tazama Live TV, Movies, Sports Tanzania yote mahali pamoja"),
        OnboardingPage("📺", "CHANNELS 100+ ZA BURE", "Azam, NBC, Clouds, TBC na nyingine nyingi bila malipo"),
        OnboardingPage("⚽", "SPORTS LIVE KILA WAKATI", "NBC Premier League, AFCON, EPL — usikose mechi yoyote"),
        OnboardingPage("💳", "LIPIA KWA M-PESA", "Tigo, Airtel, Vodacom — rahisi na salama Tanzania")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = OnboardingAdapter(pages)
        // dots disabled

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val isLast = position == pages.size - 1
                binding.btnNext.text = if (isLast) "ANZA SASA" else "ENDELEA →"
            }
        })
    }

    private fun setupButtons() {
        binding.tvSkip.setOnClickListener { finishOnboarding() }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        lifecycleScope.launch {
            tokenManager.setOnboardingDone()
            startActivity(Intent(this@OnboardingActivity, AuthActivity::class.java))
            finish()
        }
    }
}
