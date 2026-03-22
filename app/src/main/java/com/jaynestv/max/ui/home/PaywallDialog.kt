package com.jaynestv.max.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaynestv.max.databinding.DialogPaywallBinding
import com.jaynestv.max.ui.malipo.MalipoActivity

class PaywallDialog(
    private val channelName: String,
    private val onLipa: () -> Unit
) : BottomSheetDialogFragment() {

    private var _b: DialogPaywallBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        DialogPaywallBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.txtChannelName.text = "📺  $channelName"

        // Packages — bonyeza moja highlight
        val pkgs = listOf(b.pkg1, b.pkg2, b.pkg3, b.pkg4)
        pkgs.forEach { pkg ->
            pkg.setOnClickListener {
                pkgs.forEach { p -> p.setBackgroundResource(com.jaynestv.max.R.drawable.bg_plan_normal) }
                pkg.setBackgroundResource(com.jaynestv.max.R.drawable.bg_plan_selected)
            }
        }
        // Default — mwezi selected
        b.pkg2.setBackgroundResource(com.jaynestv.max.R.drawable.bg_plan_selected)

        // LIPA SASA
        b.btnLipa.setOnClickListener {
            dismiss()
            startActivity(Intent(requireContext(), MalipoActivity::class.java))
        }

        // WhatsApp
        b.btnWhatsapp.setOnClickListener {
            val wa = "https://wa.me/255616393956?text=Nataka+kujiunga+JAYNES+MAX+TV"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(wa)))
        }

        // Nambari ya malipo — copy
        b.txtPayNumber.setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("number", "0616393956"))
            Toast.makeText(requireContext(), "✓ Nambari imenakiliwa", Toast.LENGTH_SHORT).show()
        }

        // Rudi
        b.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
