package com.jaynestv.max.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jaynestv.max.data.api.RetrofitClient
import com.jaynestv.max.databinding.ActivityLoginBinding
import com.jaynestv.max.ui.home.HomeActivity
import com.jaynestv.max.ui.malipo.MalipoActivity
import com.jaynestv.max.utils.Constants
import com.jaynestv.max.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        if (session.isLoggedIn()) { goHome(); return }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.btnLipa.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }
        binding.txtForgot.setOnClickListener { showResetPanel() }
        binding.txtBackToLogin.setOnClickListener { hideResetPanel() }
        binding.btnReset.setOnClickListener { doReset() }
        binding.btnEye.setOnClickListener { togglePassword() }
    }

    // ── LOGIN moja kwa moja kwa Supabase ─────────────────────────
    private fun doLogin() {
        val email = binding.inputEmail.text.toString().trim()
        val pass  = binding.inputPassword.text.toString()
        if (email.isEmpty() || pass.isEmpty()) { showError("Jaza barua pepe na nywila"); return }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val result = supabaseLogin(email, pass)
                if (result.has("access_token")) {
                    val token = result.getString("access_token")
                    val user  = result.optJSONObject("user")
                    val uid   = user?.optString("id") ?: ""
                    val meta  = user?.optJSONObject("user_metadata")
                    val name  = meta?.optString("full_name") ?: email.substringBefore("@")

                    session.saveLogin(token, email, name, uid)

                    // Check subscription kutoka jaynes-api
                    checkSubscription(email)

                } else {
                    val errMsg = result.optString("error_description")
                        .ifEmpty { result.optString("message").ifEmpty { "Barua pepe au nywila si sahihi" } }
                    showError(errMsg)
                    setLoading(false)
                }
            } catch (e: Exception) {
                showError("Hakuna mtandao au server haifikiwi")
                setLoading(false)
            }
        }
    }

    // ── Supabase Auth API moja kwa moja (HttpURLConnection) ──────
    private fun supabaseLogin(email: String, password: String): JSONObject {
        val url = URL("${Constants.SUPABASE_URL}/auth/v1/token?grant_type=password")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", Constants.SUPABASE_KEY)
            doOutput = true
            connectTimeout = 15000
            readTimeout = 20000
        }
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val response = try {
            conn.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: "{}"
        }
        return JSONObject(response)
    }

    // ── Check subscription kutoka jaynes-api.onrender.com ────────
    private suspend fun checkSubscription(email: String) {
        try {
            val resp = RetrofitClient.apiService.checkSubscription(email = email)
            val data = resp.body()
            if (data?.active == true) {
                session.saveSubscription("premium", data.endDate)
            } else {
                session.saveSubscription("free", "")
                if (session.getTrialEnd() == 0L) {
                    session.saveTrialEnd(System.currentTimeMillis() + Constants.TRIAL_MS)
                }
            }
        } catch (e: Exception) { /* Endelea hata kama check imeshindwa */ }
        runOnUiThread { goHome() }
    }

    // ── Reset Password ───────────────────────────────────────────
    private fun doReset() {
        val email = binding.inputResetEmail.text.toString().trim()
        if (email.isEmpty()) { showError("Weka barua pepe yako"); return }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val url  = URL("${Constants.SUPABASE_URL}/auth/v1/recover")
                val conn = url.openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("apikey", Constants.SUPABASE_KEY)
                    doOutput = true
                    connectTimeout = 15000
                }
                val body = JSONObject().apply { put("email", email) }
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                conn.responseCode // trigger request
                runOnUiThread { showSuccess("✅ Link imetumwa! Angalia email yako.") }
            } catch (e: Exception) {
                runOnUiThread { showError("Imeshindwa kutuma link. Jaribu tena.") }
            }
            runOnUiThread { setLoading(false) }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showResetPanel() {
        binding.loginPanel.visibility = View.GONE
        binding.resetPanel.visibility = View.VISIBLE
        hideMsg()
    }
    private fun hideResetPanel() {
        binding.resetPanel.visibility = View.GONE
        binding.loginPanel.visibility = View.VISIBLE
        hideMsg()
    }

    private fun showError(msg: String) {
        binding.msgBox.visibility = View.VISIBLE
        binding.msgBox.setBackgroundResource(com.jaynestv.max.R.drawable.bg_error_msg)
        binding.msgText.text = msg
    }
    private fun showSuccess(msg: String) {
        binding.msgBox.visibility = View.VISIBLE
        binding.msgBox.setBackgroundResource(com.jaynestv.max.R.drawable.bg_success_msg)
        binding.msgText.text = msg
    }
    private fun hideMsg() { binding.msgBox.visibility = View.GONE }

    private fun setLoading(on: Boolean) {
        binding.btnLogin.isEnabled         = !on
        binding.progressLogin.visibility   = if (on) View.VISIBLE else View.GONE
        binding.btnLoginText.visibility    = if (on) View.GONE    else View.VISIBLE
    }

    private fun togglePassword() {
        val et  = binding.inputPassword
        val pos = et.selectionEnd
        et.inputType = if (et.inputType == 129) {
            binding.btnEye.setImageResource(com.jaynestv.max.R.drawable.ic_eye_off)
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.btnEye.setImageResource(com.jaynestv.max.R.drawable.ic_eye)
            129
        }
        et.setSelection(pos)
    }
}
