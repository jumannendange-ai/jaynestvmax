package com.jaynestv.max.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.jaynestv.max.R
import com.jaynestv.max.databinding.ActivitySplashBinding
import com.jaynestv.max.ui.home.HomeActivity
import com.jaynestv.max.ui.login.LoginActivity
import com.jaynestv.max.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = android.graphics.Color.parseColor("#080808")
        window.navigationBarColor = android.graphics.Color.parseColor("#080808")

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        // Lottie animation — rings + play button kama Azam Max
        binding.lottieView.apply {
            setAnimation(R.raw.splash_animation)
            repeatCount = 0
            speed = 1.2f
            playAnimation()
        }

        // App name inaonekana baada ya animation
        binding.appName.postDelayed({
            binding.appName.visibility = View.VISIBLE
            binding.tagline.visibility = View.VISIBLE
            binding.appName.startAnimation(AnimationUtils.loadAnimation(
                this@SplashActivity, android.R.anim.fade_in))
        }, 1200)

        // Navigate baada ya ms 3000
        binding.lottieView.postDelayed({
            val dest = if (session.isLoggedIn()) HomeActivity::class.java
                       else                       LoginActivity::class.java
            startActivity(Intent(this, dest))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}
