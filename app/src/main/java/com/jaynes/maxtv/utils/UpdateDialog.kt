package com.jaynes.maxtv.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.jaynes.maxtv.data.model.AppVersion

object UpdateDialog {

    fun showForce(activity: Activity, version: AppVersion) {
        AlertDialog.Builder(activity)
            .setTitle("🔄 Sasisho Muhimu")
            .setMessage(
                "Toleo jipya ${version.versionName} linahitajika.\n\n" +
                version.changelog.joinToString("\n• ", "• ") + "\n\n" +
                "Lazima upakue toleo jipya ili kuendelea."
            )
            .setCancelable(false)
            .setPositiveButton("PAKUA SASA") { _, _ ->
                version.apkUrl?.let { url ->
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            .show()
    }

    fun showMinor(activity: Activity, version: AppVersion, onSkip: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("🆕 Toleo Jipya ${version.versionName}")
            .setMessage(
                "Vipengele vipya:\n\n" +
                version.changelog.joinToString("\n• ", "• ")
            )
            .setCancelable(false)
            .setPositiveButton("PAKUA") { _, _ ->
                version.apkUrl?.let { url ->
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                onSkip()
            }
            .setNegativeButton("Baadaye") { _, _ -> onSkip() }
            .show()
    }
}
