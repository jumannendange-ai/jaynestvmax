package com.jaynes.maxtv.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jaynes.maxtv.data.api.RetrofitClient
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.databinding.FragmentProfileBinding
import com.jaynes.maxtv.ui.auth.AuthActivity
import com.jaynes.maxtv.ui.subscription.PlansActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        loadProfile()
        setupButtons()
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = tokenManager.getUser()
            user?.let {
                binding.tvName.text  = it.fullName ?: it.email
                binding.tvEmail.text = it.email

                val plan = it.plan.uppercase()
                binding.tvPlanBadge.text = "● $plan"
                binding.tvPlanName.text  = plan

                val subEnd = it.subEnd ?: it.trialEnd ?: "—"
                binding.tvSubEnd.text = subEnd.take(10)
            }
        }
    }

    private fun setupButtons() {
        // Lipia / Renew
        binding.btnRenew.setOnClickListener {
            startActivity(Intent(requireContext(), PlansActivity::class.java))
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val token = tokenManager.getToken()
                if (token != null) {
                    try { RetrofitClient.api.logout("Bearer $token") } catch (_: Exception) {}
                }
                tokenManager.clearTokens()
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
