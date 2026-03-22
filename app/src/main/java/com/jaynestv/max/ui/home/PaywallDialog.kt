package com.jaynestv.max.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaynestv.max.databinding.DialogPaywallBinding

class PaywallDialog(
    private val channelName: String,
    private val onLipa: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var b: DialogPaywallBinding

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        DialogPaywallBinding.inflate(i, c, false).also { b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.txtChannelName.text = "📺 $channelName"
        b.btnLipa.setOnClickListener { dismiss(); onLipa() }
        b.btnClose.setOnClickListener { dismiss() }
    }
}
