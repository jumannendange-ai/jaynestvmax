package com.jaynes.maxtv.ui.subscription

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.model.PaymentRequest
import com.jaynes.maxtv.data.model.Plan
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.ActivityPlansBinding
import kotlinx.coroutines.launch

class PlansActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlansBinding
    private lateinit var tokenManager: TokenManager
    private var selectedPlan: Plan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        binding.ivBack.setOnClickListener { finish() }

        loadPlans()
        setupPayButton()
    }

    private fun loadPlans() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val res = RetrofitClient.api.getPlans()
                if (res.isSuccessful && res.body()?.success == true) {
                    val plans = res.body()!!.plans
                    binding.rvPlans.layoutManager = LinearLayoutManager(this@PlansActivity)
                    binding.rvPlans.adapter = PlansAdapter(plans) { plan ->
                        selectedPlan = plan
                        binding.btnPay.isEnabled = true
                        binding.btnPay.text = "LIPIA ${plan.name.uppercase()} — TZS ${plan.priceMonthly}"
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlansActivity, "Imeshindwa kupakua plans", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupPayButton() {
        binding.btnPay.isEnabled = false
        binding.btnPay.setOnClickListener {
            val plan = selectedPlan ?: return@setOnClickListener
            val method = when (binding.rgPayment.checkedRadioButtonId) {
                binding.rbMpesa.id  -> "mpesa"
                binding.rbTigo.id   -> "tigo"
                binding.rbAirtel.id -> "airtel"
                else                -> "mpesa"
            }
            val phone = binding.etPhone.text.toString().trim()
            initiatePayment(plan.name, method, phone)
        }
    }

    private fun initiatePayment(plan: String, method: String, phone: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val token = tokenManager.getToken() ?: return@launch
                val res = RetrofitClient.api.initiatePayment(
                    "Bearer $token",
                    PaymentRequest(plan, method, phone)
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    Toast.makeText(
                        this@PlansActivity,
                        res.body()?.message ?: "Malipo yametumwa — angalia simu yako",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@PlansActivity, res.body()?.error ?: "Imeshindwa", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlansActivity, "Hitilafu ya mtandao", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
