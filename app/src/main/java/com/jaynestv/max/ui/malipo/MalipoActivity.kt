package com.jaynestv.max.ui.malipo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaynestv.max.data.api.RetrofitClient
import com.jaynestv.max.data.models.PayMethod
import com.jaynestv.max.data.models.Plan
import com.jaynestv.max.databinding.ActivityMalipoBinding
import com.jaynestv.max.utils.Constants
import com.jaynestv.max.utils.SessionManager
import kotlinx.coroutines.launch

class MalipoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMalipoBinding
    private lateinit var session: SessionManager
    private var selectedPlan: Plan? = null
    private var selectedMethod: PayMethod? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMalipoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }
        setupPlans()
        setupMethods()
        checkCurrentSub()
    }

    private fun setupPlans() {
        val adapter = PlanAdapter(Plan.getPlans()) { plan ->
            selectedPlan = plan
            updatePayInfo()
        }
        binding.rvPlans.layoutManager = GridLayoutManager(this, 2)
        binding.rvPlans.adapter = adapter
    }

    private fun setupMethods() {
        val adapter = MethodAdapter(PayMethod.getMethods()) { method ->
            selectedMethod = method
            updatePayInfo()
        }
        binding.rvMethods.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMethods.adapter = adapter
    }

    private fun updatePayInfo() {
        val plan   = selectedPlan   ?: return
        val method = selectedMethod ?: return

        binding.payInfoCard.visibility = View.VISIBLE
        binding.txtPayAmount.text      = "TSh ${String.format("%,d", plan.price)}"
        binding.txtPayNumber.text      = method.number

        binding.txtStep1.text = "1. Fungua ${method.name} kwenye simu yako"
        binding.txtStep2.text = "2. Tuma TSh ${String.format("%,d", plan.price)} kwa: ${method.number}"
        binding.txtStep3.text = "3. Maelezo: \"${session.getEmail()} ${plan.nameSwahili}\""
        binding.txtStep4.text = "4. Piga picha risiti, wasiliana nasi WhatsApp"

        binding.btnCopyNumber.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("number", method.number))
            Toast.makeText(this, "✓ Nambari imenakiliwa", Toast.LENGTH_SHORT).show()
        }

        // Submit payment kwenye API
        binding.payInfoCard.setOnClickListener(null)
        // Ongeza button ya kutuma
        submitPaymentToApi(plan, method)
    }

    private fun submitPaymentToApi(plan: Plan, method: PayMethod) {
        // Auto-submit kwa API wakati mtumiaji anachagua plan na method
        lifecycleScope.launch {
            try {
                val body = mapOf(
                    "email"  to session.getEmail(),
                    "plan"   to plan.id,
                    "method" to method.id
                )
                RetrofitClient.apiService.submitPayment(body)
                // Imehifadhiwa kwenye API — admin atathibitisha
            } catch (e: Exception) { /* Ignore silently */ }
        }
    }

    private fun checkCurrentSub() {
        if (session.hasPremium()) {
            binding.currentSubCard.visibility = View.VISIBLE
            binding.txtSubStatus.text = "✅ Subscription PREMIUM inafanya kazi"
            binding.txtSubEnd.text    = "Inaisha: ${session.getSubEnd().take(10)}"
        } else if (session.trialActive()) {
            binding.currentSubCard.visibility = View.VISIBLE
            val mins = session.trialSecondsLeft() / 60
            binding.txtSubStatus.text = "⏱ Majaribio — dakika $mins zimebaki"
            binding.txtSubEnd.text    = "Jisajili premium ili uendelee"
        } else {
            binding.currentSubCard.visibility = View.GONE
        }
    }
}
