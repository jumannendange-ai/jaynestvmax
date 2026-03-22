package com.jaynestv.max.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jaynestv.max.databinding.ActivityAccountBinding
import com.jaynestv.max.ui.login.LoginActivity
import com.jaynestv.max.ui.malipo.MalipoActivity
import com.jaynestv.max.utils.SessionManager

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        loadProfile()

        binding.btnMalipo.setOnClickListener {
            startActivity(Intent(this, MalipoActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Toka")
                .setMessage("Una uhakika unataka kutoka?")
                .setPositiveButton("Ndio") { _, _ ->
                    session.logout()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Hapana", null)
                .show()
        }
    }

    private fun loadProfile() {
        binding.txtName.text  = session.getName().ifEmpty { "Mtumiaji" }
        binding.txtEmail.text = session.getEmail()

        when {
            session.isAdmin() -> {
                binding.txtPlanStatus.text = "👑 ADMIN"
                binding.txtPlanStatus.setTextColor(getColor(com.jaynestv.max.R.color.red))
                binding.subCard.visibility = View.GONE
            }
            session.hasPremium() -> {
                binding.txtPlanStatus.text = "✅ PREMIUM"
                binding.txtPlanStatus.setTextColor(getColor(com.jaynestv.max.R.color.green))
                binding.txtSubEnd.text = "Inaisha: ${session.getSubEnd().take(10)}"
                binding.subCard.visibility = View.VISIBLE
                binding.btnMalipo.text = "Ongeza Muda"
            }
            session.trialActive() -> {
                val mins = session.trialSecondsLeft() / 60
                binding.txtPlanStatus.text = "⏱ MAJARIBIO ($mins min)"
                binding.txtPlanStatus.setTextColor(getColor(com.jaynestv.max.R.color.gold))
                binding.subCard.visibility = View.VISIBLE
                binding.txtSubEnd.text = "Boresha ili uendelee kutazama"
                binding.btnMalipo.text = "PATA PREMIUM"
            }
            else -> {
                binding.txtPlanStatus.text = "🔒 BURE"
                binding.txtPlanStatus.setTextColor(getColor(com.jaynestv.max.R.color.muted))
                binding.subCard.visibility = View.VISIBLE
                binding.txtSubEnd.text = "Hakuna subscription inayoendelea"
                binding.btnMalipo.text = "LIPA SASA"
            }
        }
    }
}
