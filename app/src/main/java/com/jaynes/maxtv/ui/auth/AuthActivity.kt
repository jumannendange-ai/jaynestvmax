package com.jaynes.maxtv.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.model.LoginRequest
import com.jaynes.maxtv.data.model.RegisterRequest
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.ActivityAuthBinding
import com.jaynes.maxtv.ui.home.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var tokenManager: TokenManager
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupUI()
    }

    private fun setupUI() {
        // Toggle Login/Register
        binding.tvToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateMode()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            if (isLoginMode) doLogin() else doRegister()
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                toast("Weka barua pepe kwanza")
                return@setOnClickListener
            }
            doForgotPassword(email)
        }

        updateMode()
    }

    private fun updateMode() {
        if (isLoginMode) {
            binding.tvTitle.text = "Karibu Tena 👋"
            binding.tvSubtitle.text = "Ingia kwenye akaunti yako"
            binding.btnSubmit.text = "INGIA SASA"
            binding.tvToggleMode.text = "Huna akaunti? Jisajili Bure"
            binding.layoutName.visibility = View.GONE
            binding.layoutPhone.visibility = View.GONE
            binding.tvForgotPassword.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = "Jisajili Bure 🎬"
            binding.tvSubtitle.text = "Unda akaunti yako mpya"
            binding.btnSubmit.text = "JISAJILI SASA"
            binding.tvToggleMode.text = "Una akaunti tayari? Ingia"
            binding.layoutName.visibility = View.VISIBLE
            binding.layoutPhone.visibility = View.VISIBLE
            binding.tvForgotPassword.visibility = View.GONE
        }
    }

    private fun doLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Jaza barua pepe na nywila")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.login(LoginRequest(email, password))
                if (res.isSuccessful && res.body()?.success == true) {
                    val body = res.body()!!
                    tokenManager.saveTokens(body.token!!, body.refreshToken ?: "")
                    body.user?.let { tokenManager.saveUser(it) }
                    goHome()
                } else {
                    toast(res.body()?.error ?: "Imeshindwa kuingia")
                }
            } catch (e: Exception) {
                toast("Hitilafu ya mtandao. Jaribu tena.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun doRegister() {
        val name     = binding.etName.text.toString().trim()
        val phone    = binding.etPhone.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Jaza sehemu zote")
            return
        }

        if (password.length < 6) {
            toast("Nywila lazima iwe herufi 6+")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.register(
                    RegisterRequest(email, password, name, phone)
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    val body = res.body()!!
                    tokenManager.saveTokens(body.token!!, body.refreshToken ?: "")
                    body.user?.let { tokenManager.saveUser(it) }
                    goHome()
                } else {
                    toast(res.body()?.error ?: "Imeshindwa kusajili")
                }
            } catch (e: Exception) {
                toast("Hitilafu ya mtandao. Jaribu tena.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun doForgotPassword(email: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.forgotPassword(mapOf("email" to email))
                if (res.isSuccessful) {
                    toast("Angalia barua pepe yako kwa maelekezo ya reset")
                }
            } catch (e: Exception) {
                toast("Hitilafu ya mtandao")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !loading
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
