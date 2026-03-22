package com.jaynestv.max.ui.malipo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaynestv.max.data.models.PayMethod
import com.jaynestv.max.data.models.Plan
import com.jaynestv.max.databinding.ActivityMalipoBinding
import com.jaynestv.max.utils.SessionManager

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
        updatePayInfo()
        checkCurrentSub()
    }

    private fun setupPlans() {
        val planAdapter = PlanAdapter(Plan.getPlans()) { plan ->
            selectedPlan = plan
            updatePayInfo()
        }
        binding.rvPlans.layoutManager = GridLayoutManager(this, 2)
        binding.rvPlans.adapter = planAdapter
    }

    private fun setupMethods() {
        val methodAdapter = MethodAdapter(PayMethod.getMethods()) { method ->
            selectedMethod = method
            updatePayInfo()
        }
        binding.rvMethods.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMethods.adapter = methodAdapter
    }

    private fun updatePayInfo() {
        val plan   = selectedPlan
        val method = selectedMethod

        if (plan == null || method == null) {
            binding.payInfoCard.visibility = View.GONE
            return
        }

        binding.payInfoCard.visibility = View.VISIBLE
        binding.txtPayAmount.text      = "TSh ${plan.price.toFormattedString()}"
        binding.txtPayNumber.text      = method.number

        // Steps
        binding.txtStep1.text = "1. Fungua ${method.name} kwenye simu yako"
        binding.txtStep2.text = "2. Tuma TSh ${plan.price.toFormattedString()} kwa nambari: ${method.number}"
        binding.txtStep3.text = "3. Weka maelezo: \"${session.getEmail()} ${plan.nameSwahili}\""
        binding.txtStep4.text = "4. Tutawasiliana nawe mara moja baada ya kuthibitisha malipo"

        // Copy number button
        binding.btnCopyNumber.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("number", method.number))
            Toast.makeText(this, "Nambari imenakiliwa ✓", Toast.LENGTH_SHORT).show()
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

    private fun Int.toFormattedString(): String {
        return String.format("%,d", this)
    }
}
