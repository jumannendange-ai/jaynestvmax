package com.jaynes.maxtv.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.jaynes.maxtv.BuildConfig
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.model.AppVersion
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.ui.auth.AuthActivity
import com.jaynes.maxtv.ui.home.MainActivity
import com.jaynes.maxtv.ui.onboarding.OnboardingActivity
import com.jaynes.maxtv.utils.NetworkUtils
import com.jaynes.maxtv.utils.UpdateDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this)

        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            delay(1500) // Onyesha splash kidogo

            // 1. Check internet
            if (!NetworkUtils.isConnected(this@SplashActivity)) {
                showNoInternet()
                return@launch
            }

            // 2. Check update
            val updateChecked = checkForUpdate()
            if (updateChecked) return@launch // Force update — simama hapa

            // 3. Check token
            val token = tokenManager.getToken()
            val onboardingDone = tokenManager.isOnboardingDone()

            when {
                !onboardingDone -> goTo(OnboardingActivity::class.java)
                token != null   -> goTo(MainActivity::class.java)
                else            -> goTo(AuthActivity::class.java)
            }
        }
    }

    private suspend fun checkForUpdate(): Boolean {
        return try {
            val res = RetrofitClient.api.checkUpdate(BuildConfig.VERSION_CODE)
            if (res.isSuccessful && res.body()?.updateAvailable == true) {
                val version = res.body()!!.version!!
                if (version.forceUpdate) {
                    showForceUpdate(version)
                    return true
                } else {
                    showMinorUpdate(version)
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun showForceUpdate(version: AppVersion) {
        UpdateDialog.showForce(this, version)
    }

    private fun showMinorUpdate(version: AppVersion) {
        UpdateDialog.showMinor(this, version) {
            lifecycleScope.launch { navigateAfterSplash() }
        }
    }

    private fun showNoInternet() {
        // TODO: Show no internet dialog with retry
        lifecycleScope.launch {
            delay(2000)
            navigateAfterSplash()
        }
    }

    private suspend fun navigateAfterSplash() {
        val token = tokenManager.getToken()
        val onboardingDone = tokenManager.isOnboardingDone()
        when {
            !onboardingDone -> goTo(OnboardingActivity::class.java)
            token != null   -> goTo(MainActivity::class.java)
            else            -> goTo(AuthActivity::class.java)
        }
    }

    private fun goTo(cls: Class<*>) {
        startActivity(Intent(this, cls))
        finish()
    }
}
